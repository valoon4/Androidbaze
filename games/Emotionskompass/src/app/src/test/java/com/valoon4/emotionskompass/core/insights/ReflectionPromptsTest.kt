package com.valoon4.emotionskompass.core.insights

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflectionPromptsTest {
    @Test fun bothRoutesOfferThreeDistinctVoluntaryQuestions() {
        for (route in ReflectionRoute.entries) {
            val prompts = ReflectionPrompts.forRoute(route)
            assertEquals(3, prompts.size)
            assertEquals(3, prompts.map { it.id }.toSet().size)
            assertTrue(prompts.all { it.question.isNotBlank() && it.stepName.isNotBlank() })
        }
        assertFalse(ReflectionPrompts.open.first().id in ReflectionPrompts.observation.map { it.id })
    }

    @Test fun onlyUserWordsAreTransferredNeverThePromptOrPattern() {
        val answer = "Ich möchte erst beobachten, was in solchen Momenten passiert."
        val draft = ReflectionPrompts.userDraft(
            ReflectionRoute.OBSERVATION, listOf("  $answer ", "", " ")
        )
        assertEquals(answer, draft)
        ReflectionPrompts.observation.forEach {
            assertFalse(draft.contains(it.question))
        }
        assertFalse(draft.contains("Muster"))
    }

    @Test fun allSkippedQuestionsProduceEmptyUnsavableDraft() {
        assertEquals("", ReflectionPrompts.userDraft(
            ReflectionRoute.OPEN, listOf("", "  ", "")
        ))
    }

    @Test fun multipleAnswersRetainOnlyExactUserWritingAndOrder() {
        val first = "Die Situation war für mich ungewohnt."
        val second = "Vielleicht war mir Klarheit wichtig."
        assertEquals("$first\n\n$second", ReflectionPrompts.userDraft(
            ReflectionRoute.OPEN, listOf(first, "   ", " $second ")
        ))
    }

    @Test fun invalidAnswerSizesAreRejectedBeforeEditorHandoff() {
        assertThrows(IllegalArgumentException::class.java) {
            ReflectionPrompts.userDraft(ReflectionRoute.OPEN, listOf("nur eine"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            ReflectionPrompts.userDraft(
                ReflectionRoute.OBSERVATION, listOf("x".repeat(551), "", "")
            )
        }
    }
}
