package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.BodyCue
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.CheckInMode
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.checkin.NextStepIdea
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalEncryption
import com.valoon4.emotionskompass.core.journal.JournalFormat
import org.junit.Assert.*
import org.junit.Test
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

class JournalSecurityTest {
    private val key = SecretKeySpec(ByteArray(32) { (it + 1).toByte() }, "AES")

    private fun privateDraft(): CheckInDraft = CheckInDraft(
        mode = CheckInMode.VERTIEFT,
        bodyCues = setOf(BodyCue.ENGE, BodyCue.WAERME),
        emotions = mapOf("freude" to 0, "angst" to 8, "wehmut" to null),
        area = LifeArea.BEZIEHUNGEN,
        note = "Mein privater Gedanke",
        situation = "Ein Treffen wurde verschoben",
        interpretation = "Ich hätte mir mehr Klarheit gewünscht",
        needs = setOf("klarheit", "naehe"),
        insight = "Ein anderer Blick hilft",
        nextStepIdea = NextStepIdea.GESPRAECH,
        nextStepNote = "Nachfragen"
    )

    @Test fun allPersonalFieldsRoundTripWithoutLosingZeroOrNull() {
        val entry = JournalEntry("id-1", 1000, 1500, privateDraft())
        val restored = JournalFormat.decode(JournalFormat.encode(listOf(entry))).single()
        assertEquals(entry.copy(draft = entry.draft.copy(step = restored.draft.step)), restored)
        assertEquals(0, restored.draft.emotions["freude"])
        assertTrue(restored.draft.emotions.containsKey("wehmut"))
        assertNull(restored.draft.emotions["wehmut"])
    }

    @Test fun encryptedDataHasNoReadablePrivateText() {
        val raw = JournalFormat.encode(listOf(JournalEntry("id-1", 1000, 1500, privateDraft())))
        val encrypted = JournalEncryption.seal(raw, key)
        assertFalse(encrypted.contentEquals(raw))
        assertFalse(encrypted.toString(Charsets.ISO_8859_1).contains("Mein privater Gedanke"))
        assertEquals(raw.toList(), JournalEncryption.open(encrypted, key).toList())
    }

    @Test fun ciphertextUsesFreshNonceAndIsNotDeterministic() {
        val data = "Private Notiz".toByteArray()
        val left = JournalEncryption.seal(data, key)
        val right = JournalEncryption.seal(data, key)
        assertFalse(left.contentEquals(right))
        assertEquals(data.toList(), JournalEncryption.open(left, key).toList())
        assertEquals(data.toList(), JournalEncryption.open(right, key).toList())
    }

    @Test fun tamperingWithCiphertextFailsAuthentication() {
        val data = JournalEncryption.seal("Privat".toByteArray(), key)
        data[data.lastIndex] = (data.last().toInt() xor 1).toByte()
        assertThrows(Exception::class.java) { JournalEncryption.open(data, key) }
    }

    @Test fun tamperingWithHeaderIsRejected() {
        val data = JournalEncryption.seal("Privat".toByteArray(), key)
        data[0] = 1
        assertThrows(IllegalArgumentException::class.java) { JournalEncryption.open(data, key) }
    }

    @Test fun wrongKeyCannotOpenJournal() {
        val encrypted = JournalEncryption.seal("Private Nachricht".toByteArray(), key)
        val wrong = SecretKeySpec(ByteArray(32) { (it + 3).toByte() }, "AES")
        assertThrows(Exception::class.java) { JournalEncryption.open(encrypted, wrong) }
    }

    @Test fun twoEntriesWithSameIdAreRejected() {
        val e = JournalEntry("duplicate", 1000, 1000, privateDraft())
        assertThrows(IllegalArgumentException::class.java) {
            JournalFormat.encode(listOf(e, e))
        }
    }

    @Test fun unsupportedDataVersionIsRejected() {
        val bytes = JournalFormat.encode(emptyList())
        val altered = bytes.toString(Charsets.UTF_8)
            .replace("\"version\":1", "\"version\":999")
            .toByteArray()
        assertThrows(IllegalArgumentException::class.java) { JournalFormat.decode(altered) }
    }

    @Test fun invalidIntensityIsRejected() {
        val bytes = JournalFormat.encode(listOf(JournalEntry("i", 1000, 1000, privateDraft())))
        val altered = bytes.toString(Charsets.UTF_8).replace("\"freude\":0", "\"freude\":33")
            .toByteArray()
        assertThrows(IllegalArgumentException::class.java) { JournalFormat.decode(altered) }
    }

    @Test fun shortModeAndEmptyFieldsRoundTrip() {
        val draft = CheckInDraft().withMode(CheckInMode.KURZ)
            .toggleEmotion("zufriedenheit")
        val original = JournalEntry("quick", 123456, 123456, draft)
        val restored = JournalFormat.decode(JournalFormat.encode(listOf(original))).single()
        assertEquals(CheckInMode.KURZ, restored.draft.mode)
        assertEquals(setOf("zufriedenheit"), restored.draft.emotions.keys)
        assertTrue(restored.draft.needs.isEmpty())
        assertEquals("", restored.draft.insight)
    }

    @Test fun updatePreservesIdAndCreatedAtInDocument() {
        val old = JournalEntry("stable", 1000, 1000, privateDraft())
        val changed = old.copy(updatedAt = 2000, draft = old.draft.withInsight("Neu formuliert"))
        val restored = JournalFormat.decode(JournalFormat.encode(listOf(changed))).single()
        assertEquals(old.id, restored.id)
        assertEquals(old.createdAt, restored.createdAt)
        assertEquals(2000L, restored.updatedAt)
        assertEquals("Neu formuliert", restored.draft.insight)
    }

    @Test fun deletionRetainsUnrelatedEntriesAndEmptyJournalIsValid() {
        val one = JournalEntry("one", 1000, 1000, privateDraft())
        val two = JournalEntry("two", 1001, 1001, CheckInDraft().toggleEmotion("ruhe"))
        val afterDelete = JournalFormat.decode(
            JournalFormat.encode(listOf(one, two).filterNot { it.id == one.id })
        )
        assertEquals(listOf("two"), afterDelete.map { it.id })
        assertTrue(JournalFormat.decode(JournalFormat.encode(emptyList())).isEmpty())
    }

    @Test fun journalRejectsExcessivelyLargePlaintext() {
        assertThrows(IllegalArgumentException::class.java) {
            JournalEncryption.seal(ByteArray(JournalFormat.MAX_PLAINTEXT_BYTES + 1), key)
        }
    }

    @Test fun sealedEmptyJournalIsStillAuthenticated() {
        val encrypted = JournalEncryption.seal(JournalFormat.encode(emptyList()), key)
        val decoded = JournalFormat.decode(JournalEncryption.open(encrypted, key))
        assertTrue(decoded.isEmpty())
    }
}
