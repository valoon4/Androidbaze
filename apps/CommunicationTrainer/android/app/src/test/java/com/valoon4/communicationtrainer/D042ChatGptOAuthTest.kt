package com.valoon4.communicationtrainer

import android.util.Base64
import com.valoon4.communicationtrainer.inference.ChatGptSignIn
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.Signature
import java.math.BigInteger
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class D042ChatGptOAuthTest {
    @Test fun pkceUsesS256AccordingToRfc7636() {
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            ChatGptSignIn.pkceChallenge(verifier))
    }

    @Test fun firstTimeClientIdCannotBeUsedAsIssuedRegistration() {
        assertNull(ChatGptSignIn.registeredClientId("dynamic_agent_client"))
        assertNull(ChatGptSignIn.registeredClientId("oaiapp_abc!"))
        assertNull(ChatGptSignIn.registeredClientId(null))
        assertEquals("oaiapp_abcDEF12", ChatGptSignIn.registeredClientId("oaiapp_abcDEF12"))
    }

    @Test fun jwtSignatureIssuerAudienceAndNonceVerified() {
        val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.genKeyPair()
        val public = pair.public as java.security.interfaces.RSAPublicKey
        fun base64Url(bytes: ByteArray) = Base64.encodeToString(
            bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        fun bigint(bytes: BigInteger): String {
            val raw = bytes.toByteArray()
            return base64Url(if (raw.size > 1 && raw[0] == 0.toByte()) raw.copyOfRange(1, raw.size) else raw)
        }
        val jwks = JSONObject().put("keys", JSONArray().put(JSONObject()
            .put("kty", "RSA").put("alg", "RS256").put("kid", "test-key")
            .put("n", bigint(public.modulus)).put("e", bigint(public.publicExponent))))
        val now = System.currentTimeMillis() / 1000L
        val header = JSONObject().put("alg", "RS256").put("kid", "test-key")
        val payload = JSONObject()
            .put("iss", "https://auth.openai.com")
            .put("aud", "oaiapp_abcDEF12")
            .put("nonce", "unique-test-nonce")
            .put("sub", "test-sub")
            .put("iat", now).put("exp", now + 60)
        val signingInput = base64Url(header.toString().toByteArray(StandardCharsets.UTF_8)) +
            "." + base64Url(payload.toString().toByteArray(StandardCharsets.UTF_8))
        val signature = Signature.getInstance("SHA256withRSA").apply {
            initSign(pair.private)
            update(signingInput.toByteArray(StandardCharsets.US_ASCII))
        }.sign()
        val jwt = signingInput + "." + base64Url(signature)
        val verified = ChatGptSignIn.verifyIdToken(jwt, "oaiapp_abcDEF12", "unique-test-nonce", jwks)
        assertEquals("test-sub", verified.getString("sub"))
        fun mustReject(block: () -> Unit) {
            var rejected = false
            try { block() } catch (_: Exception) { rejected = true }
            assertEquals(true, rejected)
        }
        mustReject { ChatGptSignIn.verifyIdToken(jwt, "oaiapp_wrong", "unique-test-nonce", jwks) }
        mustReject { ChatGptSignIn.verifyIdToken(jwt, "oaiapp_abcDEF12", "wrong-nonce", jwks) }
        mustReject { ChatGptSignIn.verifyIdToken(jwt + "invalid", "oaiapp_abcDEF12", "unique-test-nonce", jwks) }
    }
}
