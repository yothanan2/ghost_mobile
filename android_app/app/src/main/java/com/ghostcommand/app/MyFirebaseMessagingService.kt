package com.ghostcommand.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This triggers when a message arrives and the app is in the FOREGROUND
    // OR when a "Data" payload is received in background.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("GhostLink", "📩 Message Received from: ${remoteMessage.from}")

        // 1. Check for Notification Payload (Standard alerts)
        remoteMessage.notification?.let {
            Log.d("GhostLink", "Title: ${it.title} Body: ${it.body}")
            sendNotification(it.title ?: "Ghost Alert", it.body ?: "Check App")
        }

        // 2. Check for Data Payload (Custom triggers)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("GhostLink", "Data Payload: ${remoteMessage.data}")
            // If the message is ONLY data (no notification block), force a notification here too
            if (remoteMessage.notification == null) {
                val title = remoteMessage.data["title"] ?: "Ghost Command"
                val body = remoteMessage.data["body"] ?: "New Data Received"
                sendNotification(title, body)
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "ghost_alerts_critical" // Unique ID
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // --- STEP A: CREATE CHANNEL (Vital for Android 8+) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if channel exists, if not, create it
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Ghost Alerts (Critical)", // Visible Name
                    NotificationManager.IMPORTANCE_HIGH // MAX IMPORTANCE (Makes sound/vibration)
                ).apply {
                    description = "Trade alerts and panic warnings"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("GhostLink", "✅ Notification Channel Created")
            }
        }

        // --- STEP B: BUILD NOTIFICATION ---
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning) // Standard System Icon (Safe)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For older Androids
            .setContentIntent(pendingIntent)

        // --- STEP C: SHOW IT ---
        // Use a random ID so notifications stack instead of replacing each other
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d("GhostLink", "🚀 Notification Displayed (ID: $notificationId)")
    }

    override fun onNewToken(token: String) {
        Log.d("GhostLink", "🔄 New Token: $token")
        // Ideally send this token to your server if you needed device-specific targeting
    }
}
