package com.valoon4.communicationtrainer.inference

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.valoon4.communicationtrainer.MainActivity

/**
 * User-triggered, time-bounded foreground service only while a browser-to-127.0.0.1
 * callback is pending. Helps keep Android from terminating the listening process
 * while the system browser is in front (important on low-memory devices).
 *
 * No tokens or user identity are put in a notification or service Intent.
 * The auth worker remains in an application-scoped CoroutineScope.
 */
class ChatGptKeepAliveService : Service() {
    companion object {
        private const val CHANNEL = "chatgpt_oauth_local_callback"
        private const val NOTIFICATION_ID = 4062

        fun start(context: Context) {
            val intent = Intent(context, ChatGptKeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ChatGptKeepAliveService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "CommunicationTrainer Anmeldung", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            pendingFlags
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("CommunicationTrainer: Anmeldung läuft")
            .setContentText("Lokale Browser-Rückleitung ist vorübergehend aktiv.")
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
