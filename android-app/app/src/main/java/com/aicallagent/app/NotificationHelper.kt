package com.aicallagent.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "ai_call_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "AI Call Agent",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications when AI is handling a call"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(context: Context, phoneNumber: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AI Handling Call")
            .setContentText("AI agent is speaking with $phoneNumber")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
