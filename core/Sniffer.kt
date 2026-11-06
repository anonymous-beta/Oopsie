package com.anonymousbeta.oopsie.core

import java.io.File
import java.io.FileInputStream

object Sniffer {

    private val IMAGE_EXT = setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif", "bmp")
    private val VIDEO_EXT = setOf("mp4", "3gp", "mkv", "webm", "mov", "m4v")
    private val AUDIO_EXT = setOf("opus", "m4a", "mp3", "ogg", "aac", "amr", "wav")
    private val LOCKED_EXT = setOf("enc", "tmp", "bin", "dat", "crypt", "crypt12", "crypt14")

    fun kindOf(file: File): Kind {
        val ext = file.extension.lowercase()

        val magic = magicKind(file)
        if (magic != Kind.UNKNOWN) return magic

        return when (ext) {
            in IMAGE_EXT -> Kind.IMAGE
            in VIDEO_EXT -> Kind.VIDEO
            in AUDIO_EXT -> Kind.AUDIO
            in LOCKED_EXT -> Kind.LOCKED
            "" -> Kind.UNKNOWN
            else -> Kind.UNKNOWN
        }
    }

    private fun magicKind(file: File): Kind {
        val head = ByteArray(16)
        val read = runCatching {
            FileInputStream(file).use { it.read(head) }
        }.getOrDefault(0)
        if (read < 12) return Kind.UNKNOWN

        fun at(i: Int) = head[i].toInt() and 0xFF

        // JPEG
        if (at(0) == 0xFF && at(1) == 0xD8 && at(2) == 0xFF) return Kind.IMAGE
        // PNG
        if (at(0) == 0x89 && at(1) == 0x50 && at(2) == 0x4E && at(3) == 0x47) return Kind.IMAGE
        // GIF
        if (at(0) == 0x47 && at(1) == 0x49 && at(2) == 0x46 && at(3) == 0x38) return Kind.IMAGE
        // WEBP
        if (at(0) == 0x52 && at(1) == 0x49 && at(2) == 0x46 && at(3) == 0x46 &&
            at(8) == 0x57 && at(9) == 0x45 && at(10) == 0x42 && at(11) == 0x50) return Kind.IMAGE
        // MP4 / 3GP family
        if (at(4) == 0x66 && at(5) == 0x74 && at(6) == 0x79 && at(7) == 0x70) return Kind.VIDEO
        // OGG / OPUS
        if (at(0) == 0x4F && at(1) == 0x67 && at(2) == 0x67 && at(3) == 0x53) return Kind.AUDIO

        return Kind.UNKNOWN
    }
}
