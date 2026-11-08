package com.valoon4.communicationtrainer.inference

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File

/**
 * Read-only D04 preflight. Detects a removable, mounted app-specific directory,
 * but does NOT download, copy, install, mmap or run any model.
 *
 * This check verifies only disk-space visibility, not successful native GGUF
 * loading, sufficient RAM, reliable SD read speed, or any inference quality.
 */
enum class SdStorageStatus {
    AVAILABLE_FOR_STORAGE_PROBE,
    NOT_DETECTED,
    NOT_MOUNTED,
    NOT_ENOUGH_SPACE,
    READ_FAILED
}

data class SdStorageInspection(
    val status: SdStorageStatus,
    val sdFreeBytes: Long? = null,
    val internalFreeBytes: Long? = null
)

object SdStoragePreflight {
    /** Conservative project-defined space headroom; NOT an Android system requirement. */
    const val RECOMMENDED_SD_FREE_BYTES: Long = 2L * 1024 * 1024 * 1024

    fun assess(sdDetected: Boolean, isMounted: Boolean, sdFreeBytes: Long?): SdStorageStatus {
        if (!sdDetected) return SdStorageStatus.NOT_DETECTED
        if (!isMounted) return SdStorageStatus.NOT_MOUNTED
        if (sdFreeBytes == null || sdFreeBytes < 0L) return SdStorageStatus.READ_FAILED
        return if (sdFreeBytes >= RECOMMENDED_SD_FREE_BYTES) {
            SdStorageStatus.AVAILABLE_FOR_STORAGE_PROBE
        } else {
            SdStorageStatus.NOT_ENOUGH_SPACE
        }
    }

    fun inspect(context: Context): SdStorageInspection {
        val internalFree = freeSpaceOrNull(context.filesDir)
        // getExternalFilesDirs() provides app-specific locations, *not* raw SD roots.
        // Only use a genuinely removable volume; emulated "external" is internal.
        val dirs = try {
            context.getExternalFilesDirs(null).filterNotNull()
        } catch (_: Exception) {
            emptyList()
        }
        val removable = dirs.firstOrNull { directory ->
            try {
                Environment.isExternalStorageRemovable(directory)
            } catch (_: Exception) {
                false
            }
        } ?: return SdStorageInspection(
            status = SdStorageStatus.NOT_DETECTED,
            internalFreeBytes = internalFree
        )

        val mounted = try {
            Environment.getExternalStorageState(removable) == Environment.MEDIA_MOUNTED
        } catch (_: Exception) {
            false
        }
        if (!mounted) {
            return SdStorageInspection(
                status = SdStorageStatus.NOT_MOUNTED,
                internalFreeBytes = internalFree
            )
        }
        val sdFree = freeSpaceOrNull(removable)
        return SdStorageInspection(
            status = assess(sdDetected = true, isMounted = true, sdFreeBytes = sdFree),
            sdFreeBytes = sdFree,
            internalFreeBytes = internalFree
        )
    }

    private fun freeSpaceOrNull(directory: File): Long? = try {
        // No model files are created here and no broad storage permission is used.
        if (!directory.exists()) null else StatFs(directory.absolutePath).availableBytes
    } catch (_: Exception) {
        null
    }

    fun gib(bytes: Long): String = String.format(
        java.util.Locale.GERMANY,
        "%.1f GiB",
        bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
    )
}
