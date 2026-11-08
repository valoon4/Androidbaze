package com.valoon4.communicationtrainer

import com.valoon4.communicationtrainer.inference.QwenSdProbe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream

class D041QwenSdProbeTest {
    @Test fun rejectsLowRamEvenWhenSdHasSpace() {
        assertNotNull(QwenSdProbe.deviceGate(true, true, 2L * 1024 * 1024 * 1024, 2L * 1024 * 1024 * 1024))
        assertNotNull(QwenSdProbe.deviceGate(true, false, 1000L * 1024 * 1024, 2L * 1024 * 1024 * 1024))
    }

    @Test fun refusesNonArm64AndLowInternalReserve() {
        assertNotNull(QwenSdProbe.deviceGate(false, false, 2L * 1024 * 1024 * 1024, 2L * 1024 * 1024 * 1024))
        assertNotNull(QwenSdProbe.deviceGate(true, false, 2L * 1024 * 1024 * 1024, 300L * 1024 * 1024))
    }

    @Test fun acceptsAdequateResourcesWithoutClaimingModelQuality() {
        assertNull(QwenSdProbe.deviceGate(true, false, 1500L * 1024 * 1024, 1000L * 1024 * 1024))
    }

    @Test fun sha256IsStableAndCountsBytes() {
        val (sha, count) = QwenSdProbe.calculateSha256(ByteArrayInputStream("abc".toByteArray()))
        assertEquals(3L, count)
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", sha)
    }
}
