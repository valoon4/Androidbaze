package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.journal.BoundedJournalInput
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

class BoundedJournalInputTest {
    @Test fun emptyFileReadsAsEmpty() {
        assertArrayEquals(byteArrayOf(), BoundedJournalInput.read(ByteArrayInputStream(byteArrayOf()), 64))
    }

    @Test fun readsFileExactlyAtAllowedLimit() {
        val content = ByteArray(16384) { (it and 255).toByte() }
        assertArrayEquals(content, BoundedJournalInput.read(ByteArrayInputStream(content), 16384))
    }

    @Test fun refusesOneByteOverLimitBeforeAllocatingWholeFile() {
        val content = ByteArray(16385) { 71 }
        assertThrows(IllegalArgumentException::class.java) {
            BoundedJournalInput.read(ByteArrayInputStream(content), 16384)
        }
    }

    @Test fun rejectsNonPositiveReadLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            BoundedJournalInput.read(ByteArrayInputStream(byteArrayOf(1)), 0)
        }
    }

    @Test fun doesNotTrustProviderAvailableEstimate() {
        val content = byteArrayOf(11, 22, 33, 44, 55)
        val stream = object : ByteArrayInputStream(content) {
            override fun available(): Int = 0
        }
        assertArrayEquals(content, BoundedJournalInput.read(stream, 5))
    }

    @Test fun streamingReadHandlesProvidersReturningZeroBytesTemporarily() {
        val content = byteArrayOf(41, 42, 43)
        var returnedZero = false
        val stream = object : ByteArrayInputStream(content) {
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (!returnedZero) {
                    returnedZero = true
                    return 0
                }
                return super.read(b, off, len)
            }
        }
        assertArrayEquals(content, BoundedJournalInput.read(stream, 3))
    }

    @Test fun overLimitReadFailsForInputThatNeverSignalsFileSize() {
        val stream = object : InputStream() {
            override fun read(): Int = 0x17
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                java.util.Arrays.fill(b, off, off + len, 0x17.toByte())
                return len
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            BoundedJournalInput.read(stream, 1_024)
        }
    }
}
