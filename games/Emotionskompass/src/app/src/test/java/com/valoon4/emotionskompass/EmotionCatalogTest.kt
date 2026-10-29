package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.model.EmotionCatalog
import org.junit.Assert.*
import org.junit.Test

class EmotionCatalogTest {
    @Test fun allIdsUnique() {
        assertEquals(EmotionCatalog.emotions.size, EmotionCatalog.emotions.map { it.id }.toSet().size)
    }
    @Test fun allReferencedTermsExist() {
        val all = EmotionCatalog.emotions.map { it.id }.toSet()
        assertTrue(EmotionCatalog.emotions.flatMap { it.related }.all { it in all })
    }
    @Test fun searchRanksExactNameBeforeDefinitionHits() {
        val result = EmotionCatalog.search("Frust")
        assertEquals("frustration", result.firstOrNull()?.id)
    }
    @Test fun searchIsCaseInsensitive() {
        assertEquals(EmotionCatalog.search("wehmut"), EmotionCatalog.search("WEHMUT"))
    }
    @Test fun familyFilterWorksWithoutNetwork() {
        assertTrue(EmotionCatalog.search("", "Freude").all { it.family == "Freude" })
    }
    @Test fun unknownIdReturnsNull() {
        assertNull(EmotionCatalog.find("not-existent"))
    }
}
