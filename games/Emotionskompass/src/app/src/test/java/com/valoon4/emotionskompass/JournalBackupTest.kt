package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalFormat
import org.junit.Assert.*
import org.junit.Test

class JournalBackupTest {
    private fun password() = "Eine-laengere-Passphrase-2026!".toCharArray()
    private val original by lazy {
        JournalFormat.encode(listOf(JournalEntry("journal-1", 123L, 124L,
            CheckInDraft().toggleEmotion("freude")
                .withIntensity("freude", 0)
                .toggleNeed("naehe")
                .withInsight("Diese Erkenntnis ist privat."))))
    }

    @Test fun backupRestoresAllFieldsAndStableMetadata() {
        val secret = JournalBackup.seal(original, password())
        val restored = JournalFormat.decode(JournalBackup.open(secret, password())).single()
        assertEquals("journal-1", restored.id)
        assertEquals(123L, restored.createdAt)
        assertEquals(124L, restored.updatedAt)
        assertEquals(0, restored.draft.emotions["freude"])
        assertEquals(setOf("naehe"), restored.draft.needs)
        assertEquals("Diese Erkenntnis ist privat.", restored.draft.insight)
    }

    @Test fun plaintextAndPasswordAreNotInPortableFile() {
        val secret = JournalBackup.seal(original, password())
        val text = secret.toString(Charsets.ISO_8859_1)
        assertFalse(text.contains("Diese Erkenntnis ist privat."))
        assertFalse(text.contains("Eine-laengere-Passphrase-2026!"))
        assertFalse(secret.contentEquals(original))
    }

    @Test fun differentExportsUseRandomSaltsAndNonces() {
        val left = JournalBackup.seal(original, password())
        val right = JournalBackup.seal(original, password())
        assertFalse(left.contentEquals(right))
        assertTrue(JournalBackup.open(left, password()).contentEquals(original))
        assertTrue(JournalBackup.open(right, password()).contentEquals(original))
    }

    @Test fun wrongPassphraseCannotDecrypt() {
        val bytes = JournalBackup.seal(original, password())
        assertThrows(Exception::class.java) {
            JournalBackup.open(bytes, "Ein-anderes-geheimes-Passwort".toCharArray())
        }
    }

    @Test fun ciphertextTamperingIsDetected() {
        val bytes = JournalBackup.seal(original, password())
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        assertThrows(Exception::class.java) { JournalBackup.open(bytes, password()) }
    }

    @Test fun headerTamperingIsDetected() {
        val bytes = JournalBackup.seal(original, password())
        bytes[0] = 0
        assertThrows(IllegalArgumentException::class.java) {
            JournalBackup.open(bytes, password())
        }
    }

    @Test fun shortPassphrasesRejectedForNewExports() {
        assertThrows(IllegalArgumentException::class.java) {
            JournalBackup.seal(original, "kurz".toCharArray())
        }
    }

    @Test fun fileSizeAndTruncatedEnvelopeAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            JournalBackup.open(byteArrayOf(1, 2, 3), password())
        }
        assertThrows(IllegalArgumentException::class.java) {
            JournalBackup.open(ByteArray(JournalBackup.MAX_BACKUP_BYTES + 1), password())
        }
    }

    @Test fun malformedJournalStillRejectedAfterCorrectDecryption() {
        val encrypted = JournalBackup.seal("nicht json".toByteArray(), password())
        val raw = JournalBackup.open(encrypted, password())
        assertThrows(Exception::class.java) { JournalFormat.decode(raw) }
    }

    @Test fun encryptedEmptyJournalIsValid() {
        val raw = JournalFormat.encode(emptyList())
        val backup = JournalBackup.seal(raw, password())
        assertTrue(JournalFormat.decode(JournalBackup.open(backup, password())).isEmpty())
    }
}
