package com.valoon4.emotionskompass.core.journal

import android.content.Context
import android.util.AtomicFile
import java.io.File

/**
 * Crash-recoverable two-file EKB2 restore. Recovery snapshots contain ONLY
 * already Keystore-encrypted on-device files, never plaintext or passphrases.
 * The marker is committed last and removed only after both writes succeed.
 */
class PersonalRestoreTransaction(context: Context) {
    private val root = context.filesDir
    private val originals = listOf(
        File(root, "private_journal_v1.enc"),
        File(root, "private_personal_insights_v1.enc")
    )
    private val snapshots = listOf(
        File(root, "private_journal_v1.restore-old.enc"),
        File(root, "private_insights_v1.restore-old.enc")
    )
    private val flag = AtomicFile(File(root, "personal_restore_v2.marker"))
    private val signature = "EKT1".toByteArray(Charsets.US_ASCII)

    @Synchronized
    fun recoverIfNeeded() {
        val markerFile = flag.baseFile
        if (!markerFile.exists() && !File(markerFile.path + ".bak").exists()) {
            snapshots.forEach { AtomicFile(it).delete() }
            return
        }
        val manifest = flag.openRead().use { it.readBytes() }
        require(manifest.size == 5 && manifest.copyOfRange(0, 4).contentEquals(signature))
        val mask = manifest[4].toInt() and 0xff
        require(mask in 0..3)
        originals.forEachIndexed { index, target ->
            val originalExisted = (mask and (1 shl index)) != 0
            val dest = AtomicFile(target)
            if (originalExisted) {
                val snapshot = AtomicFile(snapshots[index])
                val cipher = snapshot.openRead().use {
                    BoundedJournalInput.read(it, if (index == 0)
                        JournalFormat.MAX_PLAINTEXT_BYTES + 128
                    else com.valoon4.emotionskompass.core.insights.PersonalInsightFormat.MAX_PLAINTEXT_BYTES + 128)
                }
                try {
                    val output = dest.startWrite()
                    try {
                        output.write(cipher)
                        dest.finishWrite(output)
                    } catch (e: Exception) {
                        dest.failWrite(output)
                        throw e
                    }
                } finally { cipher.fill(0) }
            } else {
                dest.delete()
            }
        }
        flag.delete()
        snapshots.forEach { AtomicFile(it).delete() }
    }

    /** Both repositories must have authenticated current data before calling. */
    @Synchronized
    fun replaceTogether(replace: () -> Unit) {
        recoverIfNeeded()
        var mask = 0
        originals.forEachIndexed { index, source ->
            val sourceAtomic = AtomicFile(source)
            val exists = source.exists() || File(source.path + ".bak").exists()
            if (exists) {
                mask = mask or (1 shl index)
                val encrypted = sourceAtomic.openRead().use {
                    BoundedJournalInput.read(it, if (index == 0)
                        JournalFormat.MAX_PLAINTEXT_BYTES + 128
                    else com.valoon4.emotionskompass.core.insights.PersonalInsightFormat.MAX_PLAINTEXT_BYTES + 128)
                }
                try {
                    val backup = AtomicFile(snapshots[index])
                    val writer = backup.startWrite()
                    try {
                        writer.write(encrypted)
                        backup.finishWrite(writer)
                    } catch (e: Exception) {
                        backup.failWrite(writer)
                        throw e
                    }
                } finally { encrypted.fill(0) }
            }
        }
        val writer = flag.startWrite()
        try {
            writer.write(signature + byteArrayOf(mask.toByte()))
            flag.finishWrite(writer)
        } catch (e: Exception) {
            flag.failWrite(writer)
            throw e
        }
        try {
            replace()
            // Commit: after this marker is removed, the new pair is authoritative.
            flag.delete()
            snapshots.forEach { AtomicFile(it).delete() }
        } catch (e: Exception) {
            try { recoverIfNeeded() }
            catch (recovery: Exception) { e.addSuppressed(recovery) }
            throw e
        }
    }
}
