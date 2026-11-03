package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.journal.JournalFormat
import org.junit.Assert.*
import org.junit.Test

class StrategyBundleBackupTest {
    private val now = 1_780_000_000_000L

    private fun contents(): StrategyBackupContents {
        val journal = JournalFormat.newEntry(
            CheckInDraft(note = "Fiktives Tagebuch – keine realen Daten"), now
        )
        val insight = PersonalInsight.create(
            "Ich selbst wähle eine Möglichkeit für später.", now, setOf(journal.id)
        )
        val experiment = StrategyExperiment.fromOwnIdea(
            "Eine kleine Pause in Ruhe planen", now
        ).copy(
            planText = "Ich könnte nach dem Mittagessen eine Pause machen.",
            cautionText = "Ich entscheide selbst, ob das passt.",
            status = ExperimentStatus.NOT_TRIED,
            evaluation = null,
            reflectionText = "Noch nichts ausprobiert – das ist auch okay.",
            relatedJournalIds = setOf(journal.id),
            relatedInsightIds = setOf(insight.id)
        )
        return StrategyBackupContents(listOf(journal), listOf(insight), listOf(experiment))
    }

    @Test fun eks1CodecRoundtripsUserTextAndNeutralStatuses() {
        val original = requireNotNull(contents().experiments)
        val encoded = StrategyExperimentFormat.encode(original)
        assertEquals(original, StrategyExperimentFormat.decode(encoded))
        assertEquals(ExperimentStatus.NOT_TRIED, original.single().status)
        assertNull(original.single().evaluation)
    }

    @Test fun ekb3EncryptedRoundtripContainsAllThreeAreasAndFreshNonces() {
        val original = contents()
        val password = "Nur-fiktive-Testdaten-2026!".toCharArray()
        try {
            val encoded = StrategyBundleFormat.encode(original)
            val first = StrategyBundleBackup.seal(encoded, password)
            val second = StrategyBundleBackup.seal(encoded, password)
            assertEquals("EKB3", first.copyOfRange(0, 4).decodeToString())
            assertFalse(first.contentEquals(second))
            assertEquals(original, StrategyBundleBackup.open(first, password))
        } finally { password.fill('0') }
    }

    @Test fun legacyEkb1AndEkb2AreMarkedAsMissingNotEmptyExperiments() {
        val original = contents()
        val password = "Nur-fiktive-Testdaten-2026!".toCharArray()
        try {
            val legacy = JournalBackup.seal(JournalFormat.encode(original.journal), password)
            val v1 = StrategyBundleBackup.open(legacy, password)
            assertEquals(original.journal, v1.journal)
            assertNull(v1.insights)
            assertNull(v1.experiments)

            val v2Clear = InsightBundleFormat.encode(original.journal, requireNotNull(original.insights))
            val v2 = StrategyBundleBackup.open(InsightBundleBackup.seal(v2Clear, password), password)
            assertEquals(original.journal, v2.journal)
            assertEquals(original.insights, v2.insights)
            assertNull(v2.experiments)

            val v3Empty = StrategyBundleBackup.open(
                StrategyBundleBackup.seal(
                    StrategyBundleFormat.encode(original.copy(experiments = emptyList())), password
                ), password
            )
            assertEquals(emptyList<StrategyExperiment>(), v3Empty.experiments)
        } finally { password.fill('0') }
    }

    @Test fun badPasswordOrTamperedEkb3NeverReturnsPartialContents() {
        val password = "Nur-fiktive-Testdaten-2026!".toCharArray()
        try {
            val encoded = StrategyBundleBackup.seal(
                StrategyBundleFormat.encode(contents()), password
            )
            assertThrows(Exception::class.java) {
                StrategyBundleBackup.open(encoded, "Falscher-Testschluessel-2026!".toCharArray())
            }
            val modified = encoded.copyOf().also { it[it.lastIndex] = (it.last() + 1).toByte() }
            assertThrows(Exception::class.java) {
                StrategyBundleBackup.open(modified, password)
            }
        } finally { password.fill('0') }
    }

    @Test fun danglingJournalAndInsightReferencesBlockEkb3BeforeOverwrite() {
        val original = contents()
        val entry = requireNotNull(original.experiments).single()
        assertThrows(IllegalArgumentException::class.java) {
            StrategyBundleFormat.encode(original.copy(
                experiments = listOf(entry.copy(relatedJournalIds = setOf(java.util.UUID.randomUUID().toString())))
            ))
        }
        assertThrows(IllegalArgumentException::class.java) {
            StrategyBundleFormat.encode(original.copy(
                experiments = listOf(entry.copy(relatedInsightIds = setOf(java.util.UUID.randomUUID().toString())))
            ))
        }
    }

    @Test fun pruningBrokenLinksNeverRemovesSelfAuthoredExperimentText() {
        val entry = requireNotNull(contents().experiments).single()
        val corrected = StrategyExperimentChanges.pruneMissingReferences(
            listOf(entry), emptySet(), emptySet(), now + 10
        ).single()
        assertEquals(entry.planText, corrected.planText)
        assertEquals(entry.reflectionText, corrected.reflectionText)
        assertEquals(entry.status, corrected.status)
        assertTrue(corrected.relatedJournalIds.isEmpty())
        assertTrue(corrected.relatedInsightIds.isEmpty())
        assertEquals(now + 10, corrected.updatedAtUtcMillis)
    }

    @Test fun boundedAndStableExperimentsNeverDuplicateOnUpdate() {
        val item = requireNotNull(contents().experiments).single()
        assertEquals(1, StrategyExperimentChanges.save(listOf(item), item.copy(
            status = ExperimentStatus.TRIED,
            updatedAtUtcMillis = item.updatedAtUtcMillis + 5
        )).size)
        assertThrows(IllegalArgumentException::class.java) {
            StrategyExperimentChanges.validate(listOf(item, item))
        }
        assertThrows(IllegalArgumentException::class.java) {
            StrategyExperiment.fromOwnIdea("a".repeat(StrategyExperiment.MAX_TITLE + 1), now)
        }
    }

    @Test fun editingMustNotSilentlyReassignTheOriginalStrategySource() {
        val own = StrategyExperiment.fromOwnIdea("Eigene Fiktividee", now)
        assertThrows(IllegalArgumentException::class.java) {
            StrategyExperimentChanges.save(listOf(own), own.copy(
                customTitle = null, sourceStrategyId = "planning",
                sourceTitleFallback = "Kleine Schritte planen",
                updatedAtUtcMillis = now + 1
            ))
        }
        val catalog = StrategyExperiment.fromCatalog(
            "planning", "Kleine Schritte planen", now
        )
        assertThrows(IllegalArgumentException::class.java) {
            StrategyExperimentChanges.save(listOf(catalog), catalog.copy(
                sourceStrategyId = "grounding",
                updatedAtUtcMillis = now + 1
            ))
        }
        val edited = StrategyExperimentChanges.save(listOf(catalog), catalog.copy(
            planText = "Eigener freiwilliger Text", updatedAtUtcMillis = now + 2
        )).single()
        assertEquals("planning", edited.sourceStrategyId)
        assertEquals("Eigener freiwilliger Text", edited.planText)
    }
}
