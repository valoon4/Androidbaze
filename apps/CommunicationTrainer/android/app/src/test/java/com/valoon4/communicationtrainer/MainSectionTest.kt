package com.valoon4.communicationtrainer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainSectionTest {
    @Test
    fun d01HasExactlyTwoDestinations() {
        assertEquals(listOf(MainSection.CHATS, MainSection.PRACTICE), MainSection.entries)
    }

    @Test
    fun navigationLabelsAreDistinctAndGerman() {
        assertEquals("Chats", MainSection.CHATS.title)
        assertEquals("Üben", MainSection.PRACTICE.title)
        assertNotEquals(MainSection.CHATS.headline, MainSection.PRACTICE.headline)
        assertTrue(MainSection.entries.all { it.intro.isNotBlank() })
    }
}
