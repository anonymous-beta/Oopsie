package com.anonymousbeta.oopsie.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.FileObserver
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.anonymousbeta.oopsie.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WatchService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val observers = ArrayList<FileObserver>(64)

    @Volatile
    private var lastSweep = 0L

    override fun onCreate() {
        super.onCreate()
        running = true
        buildChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        ServiceCompat.startForeground(this, NOTIF_ID, buildNotification(), type)

        if (observers.isEmpty()) attach()
        return START_STICKY
    }

    private fun attach() {
        val dirs = Paths.candidateDirs().take(60)
        for (dir in dirs) {
            @Suppress("DEPRECATION")
            val observer = object : FileObserver(
                dir.absolutePath,
                CREATE or CLOSE_WRITE or MOVED_TO
            ) {
                override fun onEvent(event: Int, path: String?) {
                    if (path == null) return
                    val now = System.currentTimeMillis()
                    if (now - lastSweep < 1500L) return
                    lastSweep = now
                    scope.launch { runCatching { Hunter.sweep(copy = true) } }
                }
            }
            observer.startWatching()
            observers += observer
        }
    }

    private fun buildChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Oopsie watcher",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps an eye on the WhatsApp cache while Oopsie is armed."
            setShowBadge(false)
        }
        mgr.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Oopsie is watching")
            .setContentText("New view-once hits get grabbed automatically.")
            .setOngoing(true)
            .setSilent(true)
            .build()

    override fun onDestroy() {
        for (o in observers) runCatching { o.stopWatching() }
        observers.clear()
        running = false
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "oopsie_watch"
        const val NOTIF_ID = 4711

        @Volatile
        var running: Boolean = false
            private set
    }
}
