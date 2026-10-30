package com.valoon4.emotionskompass.core.learning

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.valoon4.emotionskompass.core.journal.BoundedJournalInput
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Separate encrypted learning-metadata file. No journal content, account, network
 * access or system backup; no changes to the M4/M5 journal encryption/key alias.
 */
class LearningRepository(context: Context) {
    private val file = File(context.filesDir, "learning_progress_v1.enc")
    private val atomicFile = AtomicFile(file)

    @Synchronized
    fun load(): LearningState {
        if (!file.exists() && !File(file.path + ".bak").exists()) return LearningState()
        val key = key(create = false) ?: error("Der lokale Lernschlüssel fehlt. Lernstand nicht überschrieben.")
        return try {
            val ciphertext = atomicFile.openRead().use {
                BoundedJournalInput.read(it, MAX_BYTES + 128)
            }
            try {
                val plaintext = open(ciphertext, key)
                try { LearningProgressFormat.decode(plaintext) }
                finally { plaintext.fill(0) }
            } finally { ciphertext.fill(0) }
        } catch (e: Exception) {
            throw IllegalStateException("Lernstand konnte nicht sicher gelesen werden. Bestehende Daten bleiben erhalten.", e)
        }
    }

    @Synchronized
    fun change(transform: (LearningState) -> LearningState): LearningState {
        val updated = transform(load()) // Must successfully authenticate first.
        write(updated)
        return updated
    }

    @Synchronized
    fun reset() {
        load()
        write(LearningState())
    }

    private fun write(state: LearningState) {
        val plaintext = LearningProgressFormat.encode(state)
        try {
            val sealed = seal(plaintext, key(create = true)
                ?: error("Kein Android-Lernschlüssel verfügbar"))
            try {
                val output = atomicFile.startWrite()
                try {
                    output.write(sealed)
                    atomicFile.finishWrite(output)
                } catch (e: Exception) {
                    atomicFile.failWrite(output)
                    throw e
                }
            } finally { sealed.fill(0) }
        } finally { plaintext.fill(0) }
    }

    private fun key(create: Boolean): SecretKey? {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        if (!create) return null
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).setUserAuthenticationRequired(false).build())
        return generator.generateKey()
    }

    private fun seal(data: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        require(iv.size == 12)
        cipher.updateAAD(AAD)
        return MAGIC + iv + cipher.doFinal(data)
    }

    private fun open(data: ByteArray, key: SecretKey): ByteArray {
        require(data.size in 32..(MAX_BYTES + 128))
        require(data.copyOfRange(0, MAGIC.size).contentEquals(MAGIC))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key,
            GCMParameterSpec(128, data.copyOfRange(MAGIC.size, MAGIC.size + 12)))
        cipher.updateAAD(AAD)
        return cipher.doFinal(data, MAGIC.size + 12, data.size - MAGIC.size - 12)
    }

    companion object {
        private const val KEY_ALIAS = "emotionskompass_learning_progress_v1"
        const val MAX_BYTES = 512_000
        private val MAGIC = "EKL1".toByteArray(Charsets.US_ASCII)
        private val AAD = "com.valoon4.emotionskompass/learning/v1".toByteArray()
    }
}

/** Strict, versioned JSON format used only *inside* AES-GCM ciphertext. */
object LearningProgressFormat {
    private const val VERSION = 1
    private const val MAX_KEYS = 5000
    private val allowedKey = Regex("^[en]:[a-z0-9_-]{1,100}$")
    private val allowedStation = Regex("^[en]:[a-z0-9_-]{1,100}/[en]:[a-z0-9_-]{1,100}$")
    private val allowedArticle = Regex("^[a-z0-9-]{3,70}$")

    fun encode(state: LearningState): ByteArray {
        validate(state)
        val json = buildJsonObject {
            put("version", JsonPrimitive(VERSION))
            put("favorites", buildJsonArray {
                state.favorites.sorted().forEach { add(JsonPrimitive(it)) }
            })
            put("seen", buildJsonArray {
                state.seen.sorted().forEach { add(JsonPrimitive(it)) }
            })
            put("reviews", buildJsonObject {
                state.reviewCounts.toSortedMap().forEach { (id, count) ->
                    put(id, JsonPrimitive(count))
                }
            })
            put("stations", buildJsonArray {
                state.completedStations.sorted().forEach { add(JsonPrimitive(it)) }
            })
            put("articleBookmarks", buildJsonArray {
                state.articleBookmarks.sorted().forEach { add(JsonPrimitive(it)) }
            })
            put("readArticles", buildJsonArray {
                state.readArticles.sorted().forEach { add(JsonPrimitive(it)) }
            })
        }
        return json.toString().toByteArray(Charsets.UTF_8).also {
            require(it.size <= LearningRepository.MAX_BYTES)
        }
    }

    fun decode(bytes: ByteArray): LearningState {
        require(bytes.size <= LearningRepository.MAX_BYTES)
        val obj = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
        require(obj.getValue("version").jsonPrimitive.int == VERSION) { "Unbekannte Lernstand-Version" }
        fun values(key: String) = obj.getValue(key).jsonArray
            .map { it.jsonPrimitive.content }.toSet()
        // These fields are optional to preserve pre-M6.2 encrypted learning files.
        fun optionalValues(key: String) = obj[key]?.jsonArray
            ?.map { it.jsonPrimitive.content }?.toSet() ?: emptySet()
        val state = LearningState(
            favorites = values("favorites"),
            seen = values("seen"),
            reviewCounts = obj.getValue("reviews").jsonObject.mapValues {
                it.value.jsonPrimitive.int
            },
            completedStations = values("stations"),
            articleBookmarks = optionalValues("articleBookmarks"),
            readArticles = optionalValues("readArticles")
        )
        validate(state)
        return state
    }

    private fun validate(state: LearningState) {
        require(state.favorites.size <= MAX_KEYS && state.seen.size <= MAX_KEYS &&
            state.reviewCounts.size <= MAX_KEYS && state.completedStations.size <= MAX_KEYS &&
            state.articleBookmarks.size <= MAX_KEYS && state.readArticles.size <= MAX_KEYS)
        require(state.favorites.all(allowedKey::matches))
        require(state.seen.all(allowedKey::matches))
        require(state.reviewCounts.keys.all(allowedKey::matches))
        require(state.reviewCounts.values.all { it in 1..10000 })
        require(state.completedStations.all(allowedStation::matches))
        require(state.articleBookmarks.all(allowedArticle::matches))
        require(state.readArticles.all(allowedArticle::matches))
    }
}
