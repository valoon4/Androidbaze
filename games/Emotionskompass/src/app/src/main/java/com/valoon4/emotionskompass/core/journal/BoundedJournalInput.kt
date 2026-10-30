package com.valoon4.emotionskompass.core.journal

import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Reads an untrusted/corrupted encrypted file with a hard cap *before* allocating
 * the entire payload. The caller owns and closes the input stream.
 */
object BoundedJournalInput {
    fun read(source: InputStream, maxBytes: Int): ByteArray {
        require(maxBytes > 0)
        val output = ByteArrayOutputStream(minOf(maxBytes, 8192))
        val buffer = ByteArray(8192)
        try {
            while (true) {
                val count = source.read(buffer)
                if (count < 0) break
                if (count == 0) {
                    // InputStream may legally return 0; still make progress or end.
                    val one = source.read()
                    if (one < 0) break
                    require(output.size() < maxBytes) { "Verschlüsselte Datei ist zu groß." }
                    output.write(one)
                    continue
                }
                require(output.size().toLong() + count <= maxBytes.toLong()) {
                    "Verschlüsselte Datei ist zu groß."
                }
                output.write(buffer, 0, count)
            }
            return output.toByteArray()
        } finally {
            buffer.fill(0)
            // Avoid retaining an extra copy of private ciphertext in a reusable stream.
            output.reset()
        }
    }
}
