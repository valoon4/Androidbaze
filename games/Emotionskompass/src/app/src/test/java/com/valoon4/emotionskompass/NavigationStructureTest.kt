package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.navigation.AppUiState
import com.valoon4.emotionskompass.core.navigation.Destination
import com.valoon4.emotionskompass.core.navigation.DiscoveryAtlas
import com.valoon4.emotionskompass.core.navigation.MainSection
import org.junit.Assert.*
import org.junit.Test

/** The shared navigation hierarchy must not silently regress to a crowded bottom bar. */
class NavigationStructureTest {
    @Test fun mainNavigationHasExactlyFiveStableDestinations() {
        assertEquals(
            listOf(
                MainSection.START, MainSection.ENTDECKEN, MainSection.WISSEN,
                MainSection.TAGEBUCH, MainSection.RUECKBLICK
            ), MainSection.entries
        )
        assertEquals(5, MainSection.entries.size)
        assertFalse(MainSection.entries.any { it.name == "BEDUERFNISSE" })
    }

    @Test fun threeEqualAtlasesAreGroupedWithinDiscover() {
        assertEquals(listOf(
            DiscoveryAtlas.GEFUEHLE, DiscoveryAtlas.BEDUERFNISSE,
            DiscoveryAtlas.STRATEGIEN
        ), DiscoveryAtlas.entries)
        assertEquals(3, DiscoveryAtlas.entries.map { it.label }.toSet().size)
        assertTrue(DiscoveryAtlas.entries.all { it.label.isNotBlank() })
    }

    @Test fun defaultNavigationRemainsPrivateDataFreeStart() {
        assertEquals(MainSection.START, AppUiState().section)
        assertEquals(Destination.MAIN, AppUiState().destination)
    }

    @Test fun knowledgeAndDiscoverAreIndependentMainSections() {
        assertNotEquals(MainSection.WISSEN, MainSection.ENTDECKEN)
        assertTrue(MainSection.WISSEN in MainSection.entries)
        assertFalse(Destination.entries.any { it.name == "STRATEGIES" })
    }
}
