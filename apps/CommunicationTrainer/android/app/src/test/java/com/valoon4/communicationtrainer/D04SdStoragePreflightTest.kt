package com.valoon4.communicationtrainer

import com.valoon4.communicationtrainer.inference.SdStoragePreflight
import com.valoon4.communicationtrainer.inference.SdStorageStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure decision tests; a real SD volume requires physical-device verification. */
class D04SdStoragePreflightTest {
    @Test
    fun twentyGiBFreeAllowsStorageProbeButNotInferenceApproval() {
        val result = SdStoragePreflight.assess(
            sdDetected = true,
            isMounted = true,
            sdFreeBytes = 20L * 1024 * 1024 * 1024
        )
        assertEquals(SdStorageStatus.AVAILABLE_FOR_STORAGE_PROBE, result)
    }

    @Test
    fun smallOrMissingCardDoesNotClaimAvailableStorage() {
        assertEquals(
            SdStorageStatus.NOT_DETECTED,
            SdStoragePreflight.assess(false, false, null)
        )
        assertEquals(
            SdStorageStatus.NOT_MOUNTED,
            SdStoragePreflight.assess(true, false, null)
        )
        assertEquals(
            SdStorageStatus.NOT_ENOUGH_SPACE,
            SdStoragePreflight.assess(true, true, 1500L * 1024 * 1024)
        )
    }

    @Test
    fun unreadableCardIsNotTreatedAsUsable() {
        assertEquals(
            SdStorageStatus.READ_FAILED,
            SdStoragePreflight.assess(true, true, null)
        )
        assertEquals(
            SdStorageStatus.READ_FAILED,
            SdStoragePreflight.assess(true, true, -1L)
        )
    }

    @Test
    fun exactConservativeThresholdIsInclusive() {
        assertEquals(
            SdStorageStatus.AVAILABLE_FOR_STORAGE_PROBE,
            SdStoragePreflight.assess(true, true, SdStoragePreflight.RECOMMENDED_SD_FREE_BYTES)
        )
    }

    @Test
    fun formattingUsesMeasuredGibNotAdvertisedGb() {
        assertEquals("20,0 GiB", SdStoragePreflight.gib(20L * 1024 * 1024 * 1024))
    }
}
