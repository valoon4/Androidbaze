package com.valoon4.emotionskompass.core.insights

import java.security.MessageDigest

/** Counts only; no decrypted journal words, titles or user texts enter the UI preview. */
data class BackupImportPreview(
    val format: String,
    val journalCount: Int,
    val insightCount: Int?,
    val strategyExperimentCount: Int?,
    /** Binds a confirmed preview to exact ciphertext, preventing SAF source swaps. */
    val encryptedSha256: String
) {
    val replacesInsights get() = insightCount != null
    val replacesStrategyExperiments get() = strategyExperimentCount != null
}

internal fun encryptedBackupDigest(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes)
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
