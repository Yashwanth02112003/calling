package com.aicallagent.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telecom.TelecomManager
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class CallHandlerService : Service() {

    companion object {
        private const val TAG = "CallHandler"
        private const val REJECT_DELAY_MS = 1500L // Wait 1.5s then reject
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phone_number") ?: "Unknown"

        // Show foreground notification immediately
        NotificationHelper.createChannel(this)
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.buildNotification(this, phoneNumber)
        )

        Log.d(TAG, "Handling incoming call from: $phoneNumber")

        // Reject the call after a short delay, then trigger AI callback
        handler.postDelayed({
            rejectCall()
            triggerAICallback(phoneNumber)
        }, REJECT_DELAY_MS)

        return START_NOT_STICKY
    }

    private fun rejectCall() {
        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.endCall()
            Log.d(TAG, "Call rejected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject call: ${e.message}")
        }
    }

    private fun triggerAICallback(phoneNumber: String) {
        // Clean the phone number (remove spaces, dashes)
        val cleanNumber = phoneNumber.replace(Regex("[^+0-9]"), "")

        val url = "${AppConfig.SERVER_URL}/make_call?phone_number=$cleanNumber"
        Log.d(TAG, "Triggering AI callback: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "AI callback failed: ${e.message}")
                handler.postDelayed({ stopSelf() }, 3000)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: "No response"
                Log.d(TAG, "AI callback response (${response.code}): $body")
                response.close()

                // Stop the service after a delay
                handler.postDelayed({ stopSelf() }, 5000)
            }
        })
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
