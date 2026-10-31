package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.navigation.AppViewModel
import com.valoon4.emotionskompass.core.navigation.MainSection
import com.valoon4.emotionskompass.core.navigation.Destination
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class JournalLockTest {
    private val atlas by lazy {
        EmotionCatalog.fromJson(File("src/main/assets/catalog/emotions.de.v2.json").readText())
    }

    @Test fun enabledGateStartsLockedWithoutJournalDecryption() {
        val vm = AppViewModel(atlas, initialLockEnabled = true)
        assertTrue(vm.lockEnabled.value)
        assertFalse(vm.unlocked.value)
        assertTrue(vm.journalState.value.entries.isEmpty())
        assertFalse(vm.journalState.value.loaded)
        assertNull(vm.insightsSnapshotIfAvailable())
    }

    @Test fun successfulCredentialTransitionOpensGate() {
        val vm = AppViewModel(atlas, initialLockEnabled = true)
        vm.unlockJournal()
        assertTrue(vm.unlocked.value)
        // Unlocking without an authenticated journal read is NOT an empty snapshot.
        assertNull(vm.insightsSnapshotIfAvailable())
        vm.lockJournal()
        assertFalse(vm.unlocked.value)
        assertFalse(vm.journalState.value.loaded)
        assertNull(vm.insightsSnapshotIfAvailable())
    }

    @Test fun backgroundLockWipesPersonalDraftAndNavigation() {
        val vm = AppViewModel(atlas, initialLockEnabled = true)
        vm.unlockJournal()
        vm.openCheckin("angst")
        vm.setCheckInSituation("Ein vertraulicher Moment")
        vm.setCheckInInsight("Mein eigener Gedanke")
        assertTrue(vm.checkIn.value.hasPersonalInput)
        vm.onAppBackgrounded()
        assertFalse(vm.unlocked.value)
        assertNull(vm.insightsSnapshotIfAvailable())
        assertEquals(CheckInDraft(), vm.checkIn.value)
        assertEquals(Destination.MAIN, vm.state.value.destination)
        assertEquals(MainSection.START, vm.state.value.section)
    }

    @Test fun systemPickerDoesNotCancelAnExplicitDocumentTransfer() {
        val vm = AppViewModel(atlas, initialLockEnabled = true)
        vm.unlockJournal()
        vm.setSystemPickerInProgress(true)
        vm.onAppBackgrounded()
        assertTrue(vm.unlocked.value)
        vm.setSystemPickerInProgress(false)
        vm.onAppBackgrounded()
        assertFalse(vm.unlocked.value)
    }

    @Test fun reviewEvidenceSelectionNeverSurvivesAppLock() {
        val vm = AppViewModel(atlas, initialLockEnabled = true)
        vm.selectReviewEvidence("emotion" to "angst")
        assertNull(vm.reviewSelection.value)
        vm.unlockJournal()
        vm.selectReviewEvidence("emotion" to "angst")
        assertEquals("emotion" to "angst", vm.reviewSelection.value)
        vm.onAppBackgrounded()
        assertFalse(vm.unlocked.value)
        assertNull(vm.reviewSelection.value)
        assertNull(vm.insightsSnapshotIfAvailable())
    }

    @Test fun reviewCannotOpenEntryWithoutLoadedAuthenticatedJournal() {
        val vm = AppViewModel(atlas, initialLockEnabled = true)
        vm.openJournalEntryFromReview("entry-id")
        assertEquals(Destination.MAIN, vm.state.value.destination)
        vm.unlockJournal()
        vm.openJournalEntryFromReview("entry-id")
        assertEquals(Destination.MAIN, vm.state.value.destination)
    }

    @Test fun optionalGateDoesNotLockWhenDisabled() {
        val vm = AppViewModel(atlas)
        assertFalse(vm.lockEnabled.value)
        assertTrue(vm.unlocked.value)
        vm.onAppBackgrounded()
        assertTrue(vm.unlocked.value)
    }

    @Test fun disableGateKeepsUnlockedState() {
        val vm = AppViewModel(atlas, initialLockEnabled = true)
        vm.unlockJournal()
        vm.setLockEnabled(false)
        vm.onAppBackgrounded()
        assertTrue(vm.unlocked.value)
    }
}
