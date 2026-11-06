package com.anonymousbeta.oopsie.core

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat

object Perm {

    fun hasAllFiles(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

    fun requestAllFiles(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val direct = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${ctx.packageName}")
            )
            val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            val launched = runCatching {
                ctx.startActivity(direct)
                true
            }.getOrDefault(false)

            if (!launched) {
                runCatching { ctx.startActivity(fallback) }
                    .onFailure {
                        Toast.makeText(ctx, "Open Settings → All files access", Toast.LENGTH_LONG).show()
                    }
            }
        } else {
            Toast.makeText(ctx, "Grant storage permission from Settings", Toast.LENGTH_LONG).show()
        }
    }
}
