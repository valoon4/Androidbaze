package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import java.time.Instant
import java.time.ZoneId

/**
 * A cautious co-selection observation, never a psychological inference.
 * The denominator contains only entries eligible for the stated comparison.
 * Nothing is stored or sent off-device.
 */
data class PatternObservation(
    val key: String,
    val kind: String,
    val firstId: String,
    val secondId: String,
    val evidence: InsightEvidence,
    val distinctDays: Int
)

/** Deterministic rules operating only on explicitly selected structured fields. */
object DiscoverPatterns {
    const val MIN_ELIGIBLE = 10
    const val MIN_MATCHING = 3

    fun discover(
        snapshot: JournalSnapshot,
        window: InsightTimeWindow,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<PatternObservation> {
        val entries = snapshot.entries.filter { window.contains(it.createdAtUtcMillis) }
        val results = mutableListOf<PatternObservation>()

        fun collect(
            kind: String,
            eligible: List<InsightEntry>,
            choices: (InsightEntry) -> List<Pair<String, String>>
        ) {
            if (eligible.size < MIN_ELIGIBLE) return
            val byPair = sortedMapOf<Pair<String, String>, MutableList<InsightEntry>>(
                compareBy<Pair<String, String>> { it.first }.thenBy { it.second }
            )
            eligible.forEach { entry ->
                choices(entry).distinct().forEach { pair ->
                    byPair.getOrPut(pair) { mutableListOf() }.add(entry)
                }
            }
            byPair.forEach { (pair, matches) ->
                if (matches.size < MIN_MATCHING || matches.size * 4 < eligible.size) return@forEach
                val days = matches.map {
                    Instant.ofEpochMilli(it.createdAtUtcMillis).atZone(zone).toLocalDate()
                }.distinct().size
                if (days < 2) return@forEach
                results.add(
                    PatternObservation(
                        key = "$kind:${pair.first}:${pair.second}",
                        kind = kind,
                        firstId = pair.first,
                        secondId = pair.second,
                        evidence = InsightEvidence(
                            eligible.map { it.journalId },
                            matches.map { it.journalId }
                        ),
                        distinctDays = days
                    )
                )
            }
        }

        collect(
            "emotion-need",
            entries.filter { entry ->
                entry.emotionIntensities.isNotEmpty() &&
                    entry.needIds.any { it != CheckInDraft.UNKNOWN_NEED_ID }
            }
        ) { entry ->
            entry.emotionIntensities.keys.flatMap { emotion ->
                entry.needIds.filter { it != CheckInDraft.UNKNOWN_NEED_ID }
                    .map { need -> emotion to need }
            }
        }

        // Context is only a user-selected structured life area, never mined from notes.
        collect("emotion-area", entries.filter {
            it.emotionIntensities.isNotEmpty() && it.area != null
        }) { entry ->
            entry.emotionIntensities.keys.map { emotion ->
                emotion to checkNotNull(entry.area).name
            }
        }

        collect("emotion-pair", entries.filter { it.emotionIntensities.size >= 2 }) { entry ->
            val ids = entry.emotionIntensities.keys.sorted()
            ids.flatMapIndexed { i, first ->
                ids.drop(i + 1).map { second -> first to second }
            }
        }

        return results.sortedWith(
            compareByDescending<PatternObservation> { it.evidence.numerator }
                .thenByDescending { it.evidence.fraction ?: 0.0 }
                .thenBy { it.key }
        )
    }
}
