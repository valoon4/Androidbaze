package com.valoon4.emotionskompass.core.insights

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.valoon4.emotionskompass.core.journal.BoundedJournalInput
import com.valoon4.emotionskompass.core.journal.EncryptedJournalRepository
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * EKR2 is a *local*, device-keyed and atomically staged redo intent for all
 * three stores. It must never be exported. EKR1 recovery runs before EKR2.
 */
internal class EncryptedStrategyRestoreIntent(
    context: Context,
    private val journal: EncryptedJournalRepository,
    private val insights: EncryptedPersonalInsightRepository,
    private val experiments: EncryptedStrategyExperimentRepository
) : StrategyRestoreTransactionIo {
    private val file = File(context.filesDir, "private_strategy_restore_intent_v2.enc")
    private val atomic = AtomicFile(file)

    override fun pending(): ByteArray? {
        if (!file.exists() && !File(file.path + ".bak").exists()) return null
        val key = getKey(false) ?: throw IllegalStateException(
            "Wiederherstellungsschlüssel für Strategieideen fehlt; nichts überschreiben."
        )
        val sealed = atomic.openRead().use {
            BoundedJournalInput.read(it, StrategyBundleFormat.MAX_PLAINTEXT_BYTES + 128)
        }
        return try {
            require(sealed.size in 32..(StrategyBundleFormat.MAX_PLAINTEXT_BYTES + 128))
            require(sealed.copyOfRange(0, 4).contentEquals(MAGIC))
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key,
                GCMParameterSpec(128, sealed.copyOfRange(4, 16)))
            cipher.updateAAD(AAD)
            cipher.doFinal(sealed.copyOfRange(16, sealed.size))
        } finally { sealed.fill(0) }
    }

    override fun stage(plaintext: ByteArray) {
        // Validate and authenticate all dependent references before staging.
        StrategyBundleFormat.decode(plaintext)
        val key = getKey(true) ?: error("Wiederherstellungsschlüssel fehlt")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        require(cipher.iv.size == 12)
        cipher.updateAAD(AAD)
        val sealed = MAGIC + cipher.iv + cipher.doFinal(plaintext)
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
    }

    override fun replaceAll(contents: StrategyBackupContents) {
        journal.replaceVerified(contents.journal)
        insights.replaceVerified(requireNotNull(contents.insights))
        experiments.replaceVerified(requireNotNull(contents.experiments))
    }

    override fun clear() {
        atomic.delete()
        require(!file.exists() && !File(file.path + ".bak").exists()) {
            "Wiederherstellungsmarkierung konnte nicht entfernt werden"
        }
    }

    private fun getKey(create: Boolean): SecretKey? {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        if (!create) return null
        val gen = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        gen.init(KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false).build())
        return gen.generateKey()
    }

    private companion object {
        val MAGIC = "EKR2".toByteArray(Charsets.US_ASCII)
        val AAD = "com.valoon4.emotionskompass/pending-strategy-restore/v2".toByteArray()
        const val KEY_ALIAS = "emotionskompass_restore_transaction_v2"
    }
}
