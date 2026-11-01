package com.valoon4.emotionskompass.core.insights

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

/**
 * EKS1: separate Keystore-backed encrypted and atomically replaced local file.
 * No UI write path is enabled in M7.4.1. All production calls go through the
 * combined three-area coordinator AFTER recovery of EKR1 and EKR2.
 */
class EncryptedStrategyExperimentRepository(context: Context) {
    private val file = File(context.filesDir, "private_strategy_experiments_v1.enc")
    private val atomic = AtomicFile(file)

    @Synchronized
    fun list(): List<StrategyExperiment> {
        if (!file.exists() && !File(file.path + ".bak").exists()) return emptyList()
        val key = getKey(false) ?: throw IllegalStateException(
            "Schlüssel für eigene Strategieideen fehlt. Vorhandene Daten bleiben unverändert."
        )
        val sealed = atomic.openRead().use {
            BoundedJournalInput.read(it, StrategyExperimentFormat.MAX_BYTES + 128)
        }
        return try {
            val plaintext = open(sealed, key)
            try { StrategyExperimentFormat.decode(plaintext) }
            finally { plaintext.fill(0) }
        } catch (error: Exception) {
            throw IllegalStateException("Strategieideen konnten nicht sicher geöffnet werden.", error)
        } finally { sealed.fill(0) }
    }

    /** Intended only for the global coordinator after authenticated reads. */
    @Synchronized
    fun replaceVerified(items: List<StrategyExperiment>) {
        if ((file.exists() || File(file.path + ".bak").exists()) && getKey(false) == null)
            throw IllegalStateException("Strategieideen-Schlüssel fehlt; Import gestoppt.")
        write(items)
    }

    @Synchronized
    fun save(item: StrategyExperiment): List<StrategyExperiment> {
        val next = StrategyExperimentChanges.save(list(), item)
        write(next)
        return next
    }

    @Synchronized
    fun delete(id: String): List<StrategyExperiment> {
        val current = list()
        val next = StrategyExperimentChanges.delete(current, id)
        if (next.size != current.size) write(next)
        return next
    }

    private fun write(items: List<StrategyExperiment>) {
        val clear = StrategyExperimentFormat.encode(items)
        try {
            val key = getKey(true) ?: error("Kein sicherer Schlüssel")
            val sealed = seal(clear, key)
            try {
                val output = atomic.startWrite()
                try {
                    output.write(sealed)
                    atomic.finishWrite(output)
                } catch (e: Exception) {
                    atomic.failWrite(output)
                    throw e
                }
            } finally { sealed.fill(0) }
        } finally { clear.fill(0) }
    }

    private fun seal(bytes: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        require(cipher.iv.size == 12)
        cipher.updateAAD(AAD)
        return MAGIC + cipher.iv + cipher.doFinal(bytes)
    }

    private fun open(bytes: ByteArray, key: SecretKey): ByteArray {
        require(bytes.size in 32..(StrategyExperimentFormat.MAX_BYTES + 128))
        require(bytes.copyOfRange(0, 4).contentEquals(MAGIC))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key,
            GCMParameterSpec(128, bytes.copyOfRange(4, 16)))
        cipher.updateAAD(AAD)
        return cipher.doFinal(bytes.copyOfRange(16, bytes.size))
    }

    private fun getKey(create: Boolean): SecretKey? {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        if (!create) return null
        val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false).build())
        return generator.generateKey()
    }

    private companion object {
        val MAGIC = "EKS1".toByteArray(Charsets.US_ASCII)
        val AAD = "com.valoon4.emotionskompass/strategy-experiments/v1".toByteArray()
        const val KEY_ALIAS = "emotionskompass_private_strategy_experiments_v1"
    }
}
