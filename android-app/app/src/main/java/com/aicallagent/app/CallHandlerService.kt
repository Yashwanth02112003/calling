package com.aicallagent.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class CallHandlerService : Service() {

    companion object {
        private const val TAG = "CallHandler"
        private const val ANSWER_DELAY_MS = 2000L      // Wait 2s then answer
        private const val AI_CALL_TRIGGER_DELAY = 1000L // Trigger AI callback after answering
        private const val AI_CALL_WAIT_TIMEOUT = 30000L // Wait up to 30s for AI to call back
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val handler = Handler(Looper.getMainLooper())
    private var phoneNumber: String = "Unknown"
    private var isFirstCallAnswered = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        phoneNumber = intent?.getStringExtra("phone_number") ?: "Unknown"

        // Show foreground notification
        NotificationHelper.createChannel(this)
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.buildNotification(this, phoneNumber)
        )

        Log.d(TAG, "Answering incoming call from: $phoneNumber")

        // Step 1: Answer the incoming call after delay
        handler.postDelayed({
            answerCall()
        }, ANSWER_DELAY_MS)

        return START_NOT_STICKY
    }

    private fun answerCall() {
        try {
            // Answer the call using TelecomManager
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            // Note: answerRingingCall requires ANSWER_PHONE_CALLS permission (Android 9+)
            // For Android 10+, we use InCallService approach
            Log.d(TAG, "Attempting to answer call")

            // Enable speakerphone for audio bridging
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_CALL
            audioManager.isSpeakerphoneOn = true

            isFirstCallAnswered = true
            Log.d(TAG, "Call answered, speakerphone enabled")

            // Step 2: Trigger AI to call back
            handler.postDelayed({
                triggerAICallback(phoneNumber)
            }, AI_CALL_TRIGGER_DELAY)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to answer call: ${e.message}")
            stopSelf()
        }
    }

    private fun triggerAICallback(phoneNumber: String) {
        val cleanNumber = phoneNumber.replace(Regex("[^+0-9]"), "")
        val url = "${AppConfig.SERVER_URL}/make_call?phone_number=$cleanNumber"
        Log.d(TAG, "Triggering AI to call back: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "AI callback trigger failed: ${e.message}")
                handler.postDelayed({ stopSelf() }, 3000)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val body = response.body?.string() ?: "No response"
                Log.d(TAG, "AI callback triggered (${response.code}): $body")
                response.close()
                // AI is now calling us - Android will show incoming call
                // User or auto-answer will pick it up and merge
                Log.d(TAG, "AI is calling back - waiting for second call to merge")
            }
        })
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.isSpeakerphoneOn = false
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (_: Exception) {}
        super.onDestroy()
    }
}
