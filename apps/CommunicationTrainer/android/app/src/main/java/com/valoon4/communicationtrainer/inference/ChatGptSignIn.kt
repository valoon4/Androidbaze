package com.valoon4.communicationtrainer.inference

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.RSAPublicKeySpec
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.HttpsURLConnection

/**
 * D04.2 feasibility spike: native Android + system browser + OAuth/OIDC PKCE + loopback callback.
 *
 * This is intentionally ephemeral: no OAuth tokens, ID tokens, identities, or codes
 * are written to a file or a log, and no inference requests are made. The only
 * persisted value is an opaque random installation host ID.
 * Reauthentication is required after app restart. Production session/refresh
 * handling requires a later dedicated security review.
 */
object ChatGptSignIn {
    private const val AUTHORIZATION = "https://auth.openai.com/api/accounts/authorize"
    private const val TOKEN_ENDPOINT = "https://auth.openai.com/api/accounts/oauth/token"
    private const val JWKS_ENDPOINT = "https://auth.openai.com/.well-known/jwks.json"
    private const val ISSUER = "https://auth.openai.com"
    private const val RESOURCE = "https://api.openai.com/v1"
    private const val CALLBACK_PATH = "/auth/callback"
    private const val SCOPES = "openid profile email offline_access resource.invoke chatgpt.tokens.use.direct"
    private const val INITIAL_CLIENT_ID = "dynamic_agent_client"

    data class VerifiedConnection(val email: String?, val planUsageGranted: Boolean)
    sealed interface Outcome {
        data class Success(val connection: VerifiedConnection) : Outcome
        data class Failure(val message: String) : Outcome
    }

    internal fun randomUrlSafeBytes(size: Int): String {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    internal fun pkceChallenge(verifier: String): String {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(verifier.toByteArray(StandardCharsets.US_ASCII))
        return Base64.encodeToString(hash, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    internal fun registeredClientId(returnedId: String?): String? {
        return returnedId?.takeIf {
            it.length in 8..256 && it.startsWith("oaiapp_") &&
                it.all { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '-' }
        }
    }

    // An application-owned worker is not cancelled when Android destroys or
    // recreates a Compose activity while the external browser is on screen.
    // Foreground service keeps the local callback listener alive on low-RAM phones.
    private val worker = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val busy = AtomicBoolean(false)
    val working = MutableStateFlow(false)
    val signInOutcome = MutableStateFlow<Outcome?>(null)
    val probeOutcome = MutableStateFlow<String?>(null)
    val progress = MutableStateFlow("Bereit.")

    fun beginSignIn(context: Context) {
        begin(context, isProbe = false)
    }

    fun beginLocalBrowserTest(context: Context) {
        begin(context, isProbe = true)
    }

    private fun begin(context: Context, isProbe: Boolean) {
        if (!busy.compareAndSet(false, true)) return
        val app = context.applicationContext
        working.value = true
        signInOutcome.value = null
        if (!isProbe) ChatGptPlanProbe.clearSession()
        probeOutcome.value = null
        progress.value = "Starte geschützte Browser-Rückleitung …"
        try {
            ChatGptKeepAliveService.start(app)
        } catch (_: Exception) {
            progress.value = "Android konnte die kurzzeitige Anmelde-Schutzfunktion nicht starten."
            if (isProbe) probeOutcome.value = "Lokaler Browsertest wurde blockiert."
            else signInOutcome.value = Outcome.Failure("Android konnte die Browser-Anmeldung nicht absichern.")
            busy.set(false)
            working.value = false
            return
        }

        worker.launch {
            try {
                if (isProbe) {
                    probeOutcome.value = runLocalBrowserTest(app)
                } else {
                    signInOutcome.value = connect(app)
                }
            } catch (_: Exception) {
                if (isProbe) probeOutcome.value = "Lokaler Browser-Test fehlgeschlagen."
                else signInOutcome.value = Outcome.Failure("Die Anmeldung wurde unerwartet unterbrochen.")
            } finally {
                progress.value = "Test abgeschlossen. Die lokale Rückleitung wurde beendet."
                ChatGptKeepAliveService.stop(app)
                working.value = false
                busy.set(false)
            }
        }
    }

    // Offline-compatible end-to-end loopback test. NO account, OAuth request, model
    // request, cookies, network endpoint outside this phone, or user data.
    private suspend fun runLocalBrowserTest(context: Context): String = withContext(Dispatchers.IO) {
        try {
            ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
                server.soTimeout = 90_000
                val token = randomUrlSafeBytes(20)
                val probeUrl = "http://127.0.0.1:" + server.localPort +
                    "/local-browser-check?nonce=" + token
                progress.value = "Lokaler Prüfserver aktiv; Browser wird geöffnet."
                withContext(Dispatchers.Main) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(probeUrl))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addCategory(Intent.CATEGORY_BROWSABLE))
                }
                server.accept().use { socket ->
                    socket.soTimeout = 9_000
                    val reader = socket.getInputStream().bufferedReader(StandardCharsets.US_ASCII)
                    val request = reader.readLine()?.take(4096) ?: ""
                    for (i in 0 until 60) {
                        val header = reader.readLine() ?: break
                        if (header.isBlank()) break
                        if (header.length > 4096) return@withContext "Browser sendete ungültige Header."
                    }
                    val path = request.split(" ").getOrNull(1) ?: ""
                    val uri = Uri.parse("http://127.0.0.1" + path)
                    val matches = request.startsWith("GET ") &&
                        uri.path == "/local-browser-check" &&
                        constantTimeEquals(uri.getQueryParameter("nonce"), token)
                    val html = if (matches)
                        "<html><body><p>CommunicationTrainer: lokaler Browsertest erfolgreich. Zur App zurueckkehren.</p></body></html>"
                    else
                        "<html><body><p>CommunicationTrainer: lokale Pruefung fehlgeschlagen.</p></body></html>"
                    val bytes = html.toByteArray(StandardCharsets.UTF_8)
                    socket.getOutputStream().use { output ->
                        output.write(("HTTP/1.1 " + (if (matches) "200 OK" else "400 Bad Request") +
                            "\r\nContent-Type: text/html; charset=utf-8" +
                            "\r\nCache-Control: no-store" +
                            "\r\nContent-Security-Policy: default-src 'none'" +
                            "\r\nConnection: close" +
                            "\r\nContent-Length: " + bytes.size + "\r\n\r\n").toByteArray(StandardCharsets.US_ASCII))
                        output.write(bytes)
                        output.flush()
                    }
                    if (matches)
                        "Browser-Rückleitung erfolgreich: Dein Browser erreicht den lokalen Listener."
                    else
                        "Browser erreicht den Listener, aber die Sicherheitsprüfung stimmte nicht."
                }
            }
        } catch (_: SocketTimeoutException) {
            "Browser-Rückleitung nicht erreichbar (Zeitüberschreitung). Mit Chrome als Standardbrowser erneut prüfen; kein ChatGPT-Login nötig."
        } catch (_: Exception) {
            "Lokaler Browser-Test konnte nicht abgeschlossen werden. Keine Anmeldung wurde gestartet."
        }
    }

    private fun hostId(context: Context): String {
        // Host ID is an opaque, non-identifying UUID. Never use email/device ID.
        val prefs = context.getSharedPreferences("chatgpt_host", Context.MODE_PRIVATE)
        val existing = prefs.getString("host_id", null)
        if (existing != null && existing.startsWith("urn:uuid:")) return existing
        val created = "urn:uuid:${UUID.randomUUID()}"
        check(prefs.edit().putString("host_id", created).commit()) {
            "Host-ID konnte nicht gespeichert werden"
        }
        return created
    }

    /** Returns only a verified status; credentials are intentionally discarded. */
    private suspend fun connect(context: Context): Outcome = withContext(Dispatchers.IO) {
        try {
            val application = context.applicationContext
            val state = randomUrlSafeBytes(32)
            val nonce = randomUrlSafeBytes(32)
            val verifier = randomUrlSafeBytes(32)
            val host = hostId(application)
            ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
                server.soTimeout = 480_000
                val redirect = "http://127.0.0.1:${server.localPort}$CALLBACK_PATH"
                val parameters = mapOf(
                    "client_id" to INITIAL_CLIENT_ID,
                    "agent_name_hint" to "CommunicationTrainer",
                    "ext_agent_host_id" to host,
                    "response_type" to "code",
                    "redirect_uri" to redirect,
                    "scope" to SCOPES,
                    "resource" to RESOURCE,
                    "state" to state,
                    "nonce" to nonce,
                    "code_challenge_method" to "S256",
                    "code_challenge" to pkceChallenge(verifier)
                )
                progress.value = "Lokaler Listener aktiv – Anmeldung im Browser beginnt."
                val authUri = AUTHORIZATION + "?" + parameters.entries.joinToString("&") {
                    "${urlEncode(it.key)}=${urlEncode(it.value)}"
                }
                withContext(Dispatchers.Main) {
                    val browser = Intent(Intent.ACTION_VIEW, Uri.parse(authUri))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                    application.startActivity(browser)
                }
                val callback = server.accept().use { socket ->
                    progress.value = "Browser-Rückleitung angekommen – Berechtigung wird geprüft."
                    socket.soTimeout = 10_000
                    val reader = socket.getInputStream().bufferedReader(StandardCharsets.US_ASCII)
                    val first = reader.readLine()?.take(4096) ?: ""
                    // Drain headers (bounded), never log authorization code or headers.
                    for (index in 0 until 60) {
                        val line = reader.readLine() ?: break
                        if (line.isBlank()) break
                        if (line.length > 4096) throw AuthFailure("Ungültige lokale Callback-Header.")
                    }
                    val path = first.split(" ").getOrNull(1) ?: ""
                    val parsed = Uri.parse("http://127.0.0.1$path")
                    val validPath = first.startsWith("GET ") && parsed.path == CALLBACK_PATH
                    val validState = constantTimeEquals(parsed.getQueryParameter("state"), state)
                    val ok = validPath && validState
                    val response = if (ok) {
                        "Du kannst zu CommunicationTrainer zurückkehren. Prüfe dort das Ergebnis."
                    } else "Die Anmeldung wurde nicht akzeptiert. Kehre zur App zurück."
                    val payload = ("<!doctype html><html><head><meta charset='utf-8'>" +
                        "<meta name='referrer' content='no-referrer'></head><body>" +
                        "<p>$response</p></body></html>").toByteArray(StandardCharsets.UTF_8)
                    val output = socket.getOutputStream()
                    output.write(("HTTP/1.1 ${if (ok) "200 OK" else "400 Bad Request"}\r\n" +
                        "Content-Type: text/html; charset=utf-8\r\n" +
                        "Cache-Control: no-store\r\n" +
                        "Referrer-Policy: no-referrer\r\n" +
                        "Content-Security-Policy: default-src 'none'\r\n" +
                        "Connection: close\r\n" +
                        "Content-Length: ${payload.size}\r\n\r\n").toByteArray(StandardCharsets.US_ASCII))
                    output.write(payload)
                    output.flush()
                    if (!ok) throw AuthFailure("Ungültige Rückmeldung oder Sicherheitsprüfung (state).")
                    if (parsed.getQueryParameter("error") != null) {
                        throw AuthFailure("Anmeldung oder Berechtigung abgelehnt.")
                    }
                    val code = parsed.getQueryParameter("code")
                        ?.takeIf { it.isNotBlank() } ?: throw AuthFailure("Kein Autorisierungscode erhalten.")
                    val issuedId = registeredClientId(parsed.getQueryParameter("client_id"))
                        ?: throw AuthFailure("Keine gültige OpenAI-Clientregistrierung erhalten.")
                    code to issuedId
                }

                progress.value = "OpenAI-Identität wird über HTTPS überprüft."
                // Only the actual issued client ID is used for code exchange.
                val json = postForm(TOKEN_ENDPOINT, mapOf(
                    "grant_type" to "authorization_code",
                    "client_id" to callback.second,
                    "code" to callback.first,
                    "code_verifier" to verifier,
                    "redirect_uri" to redirect,
                    "resource" to RESOURCE
                ))
                val jwt = json.optString("id_token")
                    .takeIf { it.isNotBlank() } ?: throw AuthFailure("Kein ID-Token erhalten.")
                val identity = verifyIdToken(jwt, callback.second, nonce, getJson(JWKS_ENDPOINT))
                val scope = json.optString("scope")
                val granted = scope.split(" ").toSet()
                val canUsePlan = granted.contains("resource.invoke") &&
                    granted.contains("chatgpt.tokens.use.direct")
                if (canUsePlan) {
                    // Access token kept in process memory only, after signed ID token
                    // and explicit plan scopes are verified. Never retain refresh token.
                    if (json.optString("token_type") != "Bearer")
                        throw AuthFailure("OAuth-Zugriffstoken ist kein Bearer-Token.")
                    val accessToken = json.optString("access_token")
                        .takeIf { it.length in 20..8192 }
                        ?: throw AuthFailure("Kein gültiges OAuth-Zugriffstoken erhalten.")
                    ChatGptPlanProbe.onVerifiedSignIn(accessToken, json.optLong("expires_in", 0L))
                    if (!ChatGptPlanProbe.authenticated.value)
                        throw AuthFailure("Zugriffstoken ist für den Test abgelaufen oder nicht nutzbar.")
                }
                Outcome.Success(VerifiedConnection(identity.optString("email").takeIf { it.isNotBlank() }, canUsePlan))
            }
        } catch (_: SocketTimeoutException) {
            Outcome.Failure("Die lokale Browser-Rückleitung ist innerhalb von acht Minuten nicht angekommen. Prüfe zuerst den Browser-Selbsttest.")
        } catch (e: AuthFailure) {
            Outcome.Failure(e.message ?: "Anmeldung nicht möglich.")
        } catch (_: Exception) {
            // No raw response bodies, codes, tokens or identity values in error messages/logs.
            Outcome.Failure("Anmeldung technisch fehlgeschlagen. Android-Browser und Internetverbindung prüfen.")
        }
    }

    private class AuthFailure(message: String) : Exception(message)

    private fun constantTimeEquals(input: String?, expected: String): Boolean =
        input != null && MessageDigest.isEqual(
            input.toByteArray(StandardCharsets.UTF_8),
            expected.toByteArray(StandardCharsets.UTF_8)
        )

    private fun urlEncode(v: String): String = URLEncoder.encode(v, "UTF-8")

    private fun postForm(endpoint: String, params: Map<String, String>): JSONObject {
        val connection = (URL(endpoint).openConnection() as HttpsURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 12_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            val bytes = params.entries.joinToString("&") { "${urlEncode(it.key)}=${urlEncode(it.value)}" }
                .toByteArray(StandardCharsets.UTF_8)
            connection.outputStream.use { it.write(bytes) }
            if (connection.responseCode != 200) throw AuthFailure("OpenAI hat den Tokenaustausch abgelehnt (HTTP ${connection.responseCode}).")
            return JSONObject(readLimited(connection))
        } finally { connection.disconnect() }
    }

    private fun getJson(endpoint: String): JSONObject {
        val connection = (URL(endpoint).openConnection() as HttpsURLConnection)
        try {
            connection.connectTimeout = 12_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            if (connection.responseCode != 200) throw AuthFailure("OIDC-Schlüssel konnten nicht abgerufen werden.")
            return JSONObject(readLimited(connection))
        } finally { connection.disconnect() }
    }

    private fun readLimited(connection: HttpsURLConnection): String {
        connection.inputStream.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (total > 128 * 1024) throw AuthFailure("Unerwartet große Serverantwort.")
                output.write(buffer, 0, read)
            }
            return output.toString("UTF-8")
        }
    }

    /** Verify RS256 signature against issuer JWKS BEFORE trusting any identity claims. */
    internal fun verifyIdToken(jwt: String, clientId: String, nonce: String, jwks: JSONObject): JSONObject {
        val parts = jwt.split(".")
        if (parts.size != 3) throw AuthFailure("Ungültiges ID-Tokenformat.")
        val header = JSONObject(String(decodeUrl(parts[0]), StandardCharsets.UTF_8))
        if (header.optString("alg") != "RS256") throw AuthFailure("Nicht unterstützte ID-Token-Signatur.")
        val kid = header.optString("kid").takeIf { it.isNotBlank() }
            ?: throw AuthFailure("ID-Token ohne Schlüsselkennung.")
        val keys = jwks.optJSONArray("keys") ?: JSONArray()
        var publicKey: java.security.PublicKey? = null
        for (i in 0 until keys.length()) {
            val key = keys.getJSONObject(i)
            if (key.optString("kid") != kid || key.optString("kty") != "RSA") continue
            if (key.optString("alg", "RS256") != "RS256") continue
            val modulus = BigInteger(1, decodeUrl(key.getString("n")))
            val exponent = BigInteger(1, decodeUrl(key.getString("e")))
            publicKey = KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(modulus, exponent))
            break
        }
        if (publicKey == null) throw AuthFailure("Kein passender vertrauenswürdiger OIDC-Schlüssel.")
        val verifier = Signature.getInstance("SHA256withRSA")
        verifier.initVerify(publicKey)
        verifier.update("${parts[0]}.${parts[1]}".toByteArray(StandardCharsets.US_ASCII))
        if (!verifier.verify(decodeUrl(parts[2]))) throw AuthFailure("ID-Token-Signatur ungültig.")
        val payload = JSONObject(String(decodeUrl(parts[1]), StandardCharsets.UTF_8))
        val now = System.currentTimeMillis() / 1000L
        val audience = payload.opt("aud")
        val acceptedAudience = when (audience) {
            is String -> audience == clientId
            is JSONArray -> (0 until audience.length()).any { audience.optString(it) == clientId }
            else -> false
        }
        if (payload.optString("iss") != ISSUER || !acceptedAudience) throw AuthFailure("OIDC-Aussteller oder Client falsch.")
        if (!constantTimeEquals(payload.optString("nonce"), nonce)) throw AuthFailure("OIDC-Nonce stimmt nicht überein.")
        if (payload.optLong("exp", 0L) <= now - 5L ||
            payload.optLong("iat", Long.MAX_VALUE) > now + 300L ||
            payload.optLong("nbf", 0L) > now + 5L ||
            payload.optString("sub").isBlank()
        ) throw AuthFailure("ID-Token abgelaufen oder ungültig.")
        return payload
    }

    private fun decodeUrl(data: String): ByteArray =
        Base64.decode(data, Base64.URL_SAFE or Base64.NO_WRAP)
}
