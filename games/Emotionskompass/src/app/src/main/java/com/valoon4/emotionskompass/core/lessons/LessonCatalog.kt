package com.valoon4.emotionskompass.core.lessons

import java.time.LocalDate
import java.util.Locale
import java.text.Normalizer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int

enum class LessonTopic(val label: String) {
    GEFUEHLE("Gefühle verstehen"),
    BEDUERFNISSE("Bedürfnisse erkennen"),
    STRATEGIEN("Strategien & Kommunikation"),
    REFLEXION("Selbstwahrnehmung")
}

data class LessonSection(val title: String, val text: String)

data class LessonCheck(
    val prompt: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String
) {
    init {
        require(prompt.length >= 20 && choices.size == 2)
        require(choices.all { it.length >= 20 } && choices.distinct().size == 2)
        require(correctIndex in choices.indices && explanation.length >= 40)
    }
}

data class KnowledgeLesson(
    val id: String,
    val topic: LessonTopic,
    val title: String,
    val lead: String,
    val sections: List<LessonSection>,
    val example: String,
    val takeaway: String,
    val reflection: String,
    val sources: List<String>,
    val learningGoal: String,
    val commonMistake: String,
    val check: LessonCheck
) {
    val allText: String get() = (listOf(title, lead, example, takeaway, learningGoal, commonMistake) +
        sections.flatMap { listOf(it.title, it.text) }).joinToString(" ")
    val readingMinutes: Int get() = ((allText.split(Regex("\\s+")).size + 149) / 150).coerceAtLeast(2)
}

data class LessonSeries(
    val id: String,
    val title: String,
    val description: String,
    val lessonIds: List<String>
)

/** Only editorial, bundled offline content. Never downloaded or generated from user data. */
class LessonCatalog private constructor(
    val contentRevision: String,
    val lessons: List<KnowledgeLesson>,
    val series: List<LessonSeries>
) {
    private val byId = lessons.associateBy { it.id }
    fun find(id: String): KnowledgeLesson? = byId[id]

    /** Suggest the next unread article, but never lock earlier or later chapters. */
    fun continueAt(path: LessonSeries, read: Set<String>): KnowledgeLesson? =
        path.lessonIds.firstOrNull { it !in read }?.let(byId::get)

    fun continueSeries(read: Set<String>): LessonSeries? =
        series.firstOrNull { path ->
            path.lessonIds.any { it in read } && path.lessonIds.any { it !in read }
        } ?: series.firstOrNull { path -> path.lessonIds.any { it !in read } }
    fun within(series: LessonSeries): List<KnowledgeLesson> = series.lessonIds.mapNotNull(byId::get)

    fun browse(query: String = "", topic: LessonTopic? = null): List<KnowledgeLesson> {
        val q = normalized(query.trim())
        return lessons.filter { (topic == null || it.topic == topic) &&
            (q.isBlank() || normalized(it.allText).contains(q)) }
    }

    fun daily(day: LocalDate): KnowledgeLesson =
        lessons[Math.floorMod(day.toEpochDay(), lessons.size.toLong()).toInt()]

    companion object {
        private fun normalized(value: String): String =
            Normalizer.normalize(value.lowercase(Locale.GERMAN)
                .replace("ß", "ss"), Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")

        private val validId = Regex("^[a-z0-9-]{3,70}$")
        fun fromJson(source: String): LessonCatalog {
            val obj = Json.parseToJsonElement(source).jsonObject
            require(obj.getValue("schemaVersion").jsonPrimitive.int == 1)
            require(obj.getValue("locale").jsonPrimitive.content == "de")
            val revision = obj.getValue("contentRevision").jsonPrimitive.content
            require(revision.isNotBlank())
            val lessons = obj.getValue("lessons").jsonArray.map { entry ->
                val e = entry.jsonObject
                fun value(key: String) = e.getValue(key).jsonPrimitive.content
                KnowledgeLesson(
                    id = value("id"),
                    topic = LessonTopic.valueOf(value("topic")),
                    title = value("title"),
                    lead = value("lead"),
                    sections = e.getValue("sections").jsonArray.map {
                        val section = it.jsonObject
                        LessonSection(section.getValue("title").jsonPrimitive.content,
                            section.getValue("text").jsonPrimitive.content)
                    },
                    example = value("example"),
                    takeaway = value("takeaway"),
                    reflection = value("reflection"),
                    sources = e.getValue("sources").jsonArray.map { it.jsonPrimitive.content },
                    learningGoal = value("learningGoal"),
                    commonMistake = value("commonMistake"),
                    check = e.getValue("check").jsonObject.let { q ->
                        LessonCheck(
                            q.getValue("prompt").jsonPrimitive.content,
                            q.getValue("choices").jsonArray.map { it.jsonPrimitive.content },
                            q.getValue("correctIndex").jsonPrimitive.int,
                            q.getValue("explanation").jsonPrimitive.content
                        )
                    }
                )
            }
            require(lessons.isNotEmpty() && lessons.size <= 200)
            require(lessons.map { it.id }.distinct().size == lessons.size)
            require(lessons.all { lesson ->
                validId.matches(lesson.id) && lesson.title.isNotBlank() &&
                lesson.lead.length >= 40 && lesson.sections.size >= 3 &&
                lesson.sections.all { it.title.isNotBlank() && it.text.length >= 100 } &&
                lesson.example.length >= 60 && lesson.takeaway.length >= 40 &&
                lesson.reflection.length >= 20 &&
                lesson.learningGoal.length >= 25 && lesson.commonMistake.length >= 25 &&
                lesson.sources.isNotEmpty() && lesson.sources.all { it.startsWith("https://") }
            })
            require(LessonTopic.entries.all { topic ->
                lessons.any { it.topic == topic }
            })
            val paths = obj.getValue("series").jsonArray.map { item ->
                val e = item.jsonObject
                LessonSeries(
                    e.getValue("id").jsonPrimitive.content,
                    e.getValue("title").jsonPrimitive.content,
                    e.getValue("description").jsonPrimitive.content,
                    e.getValue("lessonIds").jsonArray.map { it.jsonPrimitive.content }
                )
            }
            val ids = lessons.map { it.id }.toSet()
            require(paths.isNotEmpty() && paths.map { it.id }.distinct().size == paths.size)
            require(paths.all { path ->
                validId.matches(path.id) && path.title.isNotBlank() &&
                path.description.isNotBlank() && path.lessonIds.isNotEmpty() &&
                path.lessonIds.distinct().size == 5 && path.lessonIds.all { it in ids }
            })
            require(paths.flatMap { it.lessonIds }.toSet() == ids)
            return LessonCatalog(revision, lessons, paths)
        }
    }
}
