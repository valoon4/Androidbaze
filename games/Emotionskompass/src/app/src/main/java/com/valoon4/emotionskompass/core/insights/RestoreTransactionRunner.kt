package com.valoon4.emotionskompass.core.insights

/**
 * Crash-replay contract. A durable, encrypted intent MUST be committed before
 * writing either encrypted destination file. Replaying the same fully validated
 * state is idempotent. Missing/damaged intent is not treated as success.
 */
internal interface RestoreTransactionIo {
    /** Returns decrypted pending bytes, if there is an intent. Caller clears bytes. */
    fun pending(): ByteArray?
    fun stage(plaintext: ByteArray)
    fun replaceBoth(contents: InsightBackupContents)
    fun clear()
}

internal object RestoreTransactionRunner {
    fun recover(io: RestoreTransactionIo): Boolean {
        val pending = io.pending() ?: return false
        try {
            val parsed = InsightBundleFormat.decode(pending)
            requireNotNull(parsed.insights) { "Restore intent requires both data areas" }
            io.replaceBoth(parsed)
            // A failure to clear must fail the operation, not permit new writes.
            io.clear()
            return true
        } finally {
            pending.fill(0)
        }
    }

    fun commit(io: RestoreTransactionIo, target: InsightBackupContents) {
        // Never replace an earlier unfinished transaction with a newer one.
        recover(io)
        val insights = requireNotNull(target.insights)
        val pending = InsightBundleFormat.encode(target.journal, insights)
        try {
            io.stage(pending)       // durable and encrypted before the first write
            check(recover(io)) { "Verschlüsselter Wiederherstellungsauftrag fehlt" }
        } finally {
            pending.fill(0)
        }
    }
}
