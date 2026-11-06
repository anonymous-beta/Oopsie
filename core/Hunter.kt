package com.anonymousbeta.oopsie.core

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Hunter {

    enum class Region { PRIVATE, STATUS, CACHE, LIBRARY, OTHER }

    data class Hit(
        val sourcePath: String,
        val kind: Kind,
        val region: Region,
        val sizeBytes: Long,
        val modified: Long,
        val copiedTo: String?
    )

    data class Report(
        val roots: List<String>,
        val dirsScanned: Int,
        val filesSeen: Int,
        val hits: List<Hit>,
        val errors: List<String>,
        val millis: Long
    ) {
        val copied: Int get() = hits.count { it.copiedTo != null }
    }

    private const val MIN_BYTES = 1024L
    private const val MAX_COPIES = 400

    fun outputDir(): File {
        @Suppress("DEPRECATION")
        val base = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val dir = File(base, "Oopsie")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun sweep(copy: Boolean = true): Report {
        val started = System.currentTimeMillis()
        val errors = ArrayList<String>(4)
        val hits = ArrayList<Hit>(64)
        var filesSeen = 0
        var copies = 0

        val roots = Paths.whatsappRoots()
        val dirs = Paths.candidateDirs()
        val out = if (copy) outputDir() else null

        for (dir in dirs) {
            val kids = runCatching { dir.listFiles() }.getOrNull() ?: continue
            for (f in kids) {
                if (!f.isFile) continue
                filesSeen++

                val kind = Sniffer.kindOf(f)
                if (kind == Kind.UNKNOWN) continue
                if (f.length() < MIN_BYTES) continue

                val region = regionOf(f.absolutePath)

                var copiedTo: String? = null
                val worthCopying = kind != Kind.LOCKED &&
                    region != Region.LIBRARY &&
                    out != null &&
                    copies < MAX_COPIES

                if (worthCopying) {
                    copiedTo = runCatching { copyOut(f, out!!) }
                        .onSuccess { copies++ }
                        .onFailure { errors += "${f.name}: ${it.message}" }
                        .getOrNull()
                }

                hits += Hit(f.absolutePath, kind, region, f.length(), f.lastModified(), copiedTo)
            }
        }

        hits.sortByDescending { it.modified }

        return Report(
            roots = roots.map { it.absolutePath },
            dirsScanned = dirs.size,
            filesSeen = filesSeen,
            hits = hits,
            errors = errors,
            millis = System.currentTimeMillis() - started
        )
    }

    private fun regionOf(path: String): Region {
        val p = path.lowercase(Locale.US)
        return when {
            p.contains("/private/") -> Region.PRIVATE
            p.contains("/.statuses/") -> Region.STATUS
            p.contains("/cache/") -> Region.CACHE
            p.contains("/whatsapp images/") ||
                p.contains("/whatsapp video/") ||
                p.contains("/whatsapp audio/") ||
                p.contains("/whatsapp documents/") -> Region.LIBRARY
            else -> Region.OTHER
        }
    }

    private fun copyOut(src: File, outDir: File): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(src.lastModified()))
        val safe = src.name.replace(Regex("[^A-Za-z0-9._-]"), "_").take(40)
        val dest = File(outDir, "oopsie_${stamp}_$safe")
        if (dest.exists() && dest.length() == src.length()) return dest.absolutePath
        src.inputStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output, 64 * 1024)
            }
        }
        return dest.absolutePath
    }
}
