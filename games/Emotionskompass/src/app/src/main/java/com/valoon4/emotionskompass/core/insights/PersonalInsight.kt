package com.valoon4.emotionskompass.core.insights

import java.util.UUID

/**
 * User-authored reflection, never inferred from a pattern or automatically saved.
 * Entries may be archived without erasing their content.
 */
data class PersonalInsight(
    val id: String,
    val createdAtUtcMillis: Long,
    val updatedAtUtcMillis: Long,
    val text: String,
    val archived: Boolean = false,
    val journalIds: Set<String> = emptySet(),
    val emotionIds: Set<String> = emptySet(),
    val needIds: Set<String> = emptySet()
) {
    init {
        require(runCatching { UUID.fromString(id) }.isSuccess) { "Invalid insight UUID" }
        require(createdAtUtcMillis > 0L && updatedAtUtcMillis >= createdAtUtcMillis)
        require(text == text.trim() && text.isNotBlank() && text.length <= MAX_TEXT_LENGTH)
        require(journalIds.size <= MAX_REFERENCES && emotionIds.size <= MAX_REFERENCES &&
            needIds.size <= MAX_REFERENCES)
        require((journalIds + emotionIds + needIds).none { it.isBlank() || it.length > 128 })
    }

    companion object {
        const val MAX_TEXT_LENGTH = 2000
        const val MAX_REFERENCES = 20
        const val MAX_INSIGHTS = 500
        fun create(text: String, now: Long, journalIds: Set<String> = emptySet()): PersonalInsight =
            PersonalInsight(UUID.randomUUID().toString(), now, now, text.trim(),
                journalIds = journalIds)
    }
}

/**
 * Deterministic, pure reducers; never mutate decrypted source lists in place.
 * Explicit confirmation belongs to the UI before delete; writes belong to encrypted storage.
 */
object PersonalInsightChanges {
    fun save(items: List<PersonalInsight>, next: PersonalInsight): List<PersonalInsight> {
        require(items.size <= PersonalInsight.MAX_INSIGHTS)
        require(items.map { it.id }.distinct().size == items.size)
        val old = items.firstOrNull { it.id == next.id }
        require(old == null || (old.createdAtUtcMillis == next.createdAtUtcMillis &&
            next.updatedAtUtcMillis >= old.updatedAtUtcMillis))
        require(old != null || items.size < PersonalInsight.MAX_INSIGHTS)
        return (items.filterNot { it.id == next.id } + next).sortedWith(
            compareByDescending<PersonalInsight> { it.updatedAtUtcMillis }.thenBy { it.id }
        )
    }

    fun delete(items: List<PersonalInsight>, id: String): List<PersonalInsight> =
        items.filterNot { it.id == id }

    /** Journal deletion removes broken links, but never silently deletes personal writing. */
    fun removeJournalReference(items: List<PersonalInsight>, journalId: String,
                               now: Long): List<PersonalInsight> =
        items.map { item ->
            if (journalId in item.journalIds)
                item.copy(journalIds = item.journalIds - journalId,
                    updatedAtUtcMillis = maxOf(now, item.updatedAtUtcMillis))
            else item
        }

    fun search(items: List<PersonalInsight>, query: String, includeArchived: Boolean = false):
        List<PersonalInsight> {
        val q = query.trim()
        return items.filter { (includeArchived || !it.archived) &&
            (q.isEmpty() || it.text.contains(q, ignoreCase = true)) }
            .sortedWith(compareByDescending<PersonalInsight> { it.updatedAtUtcMillis }
                .thenBy { it.id })
    }
}
