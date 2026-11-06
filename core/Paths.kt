package com.anonymousbeta.oopsie.core

import android.os.Environment
import java.io.File
import java.nio.file.Files

object Paths {

    private const val MAX_DEPTH = 8
    private const val MAX_DIRS = 400

    private val PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

    fun whatsappRoots(): List<File> {
        val base = Environment.getExternalStorageDirectory()
        val out = LinkedHashSet<File>()
        for (pkg in PACKAGES) {
            out += File(base, "Android/data/$pkg")
            out += File(base, "Android/media/$pkg")
        }
        out += File(base, "WhatsApp")
        out += File(base, "WhatsApp Business")
        return out.filter { it.exists() }
    }

    fun candidateDirs(): List<File> {
        val sink = ArrayList<File>(64)
        for (root in whatsappRoots()) {
            walk(root, 0, sink)
            if (sink.size >= MAX_DIRS) break
        }
        return sink
    }

    private fun walk(dir: File, depth: Int, sink: MutableList<File>) {
        if (depth > MAX_DEPTH || sink.size >= MAX_DIRS) return
        val isLink = runCatching { Files.isSymbolicLink(dir.toPath()) }.getOrDefault(false)
        if (isLink) return
        sink += dir
        val kids = runCatching { dir.listFiles() }.getOrNull() ?: return
        for (kid in kids) {
            if (kid.isDirectory) walk(kid, depth + 1, sink)
            if (sink.size >= MAX_DIRS) return
        }
    }
}
