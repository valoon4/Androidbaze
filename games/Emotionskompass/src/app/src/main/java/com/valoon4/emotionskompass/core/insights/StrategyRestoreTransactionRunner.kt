package com.valoon4.emotionskompass.core.insights

/**
 * Three encrypted destination files cannot be committed as a filesystem
 * transaction. This complete, encrypted redo target is durable before any
 * of the three destinations is touched. Replaying it is idempotent.
 */
internal interface StrategyRestoreTransactionIo {
    fun pending(): ByteArray?
    fun stage(plaintext: ByteArray)
    fun replaceAll(contents: StrategyBackupContents)
    fun clear()
}

internal object StrategyRestoreTransactionRunner {
    fun recover(io: StrategyRestoreTransactionIo): Boolean {
        val pending = io.pending() ?: return false
        try {
            val parsed = StrategyBundleFormat.decode(pending)
            requireNotNull(parsed.insights)
            requireNotNull(parsed.experiments)
            io.replaceAll(parsed)
            io.clear()
            return true
        } finally { pending.fill(0) }
    }

    fun commit(io: StrategyRestoreTransactionIo, contents: StrategyBackupContents) {
        recover(io) // never overwrite a previous pending redo
        val pending = StrategyBundleFormat.encode(contents)
        try {
            io.stage(pending)
            check(recover(io)) { "Verschlüsselter Wiederherstellungsauftrag fehlt" }
        } finally { pending.fill(0) }
    }
}
