package com.aicallagent.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log

class CallDetectionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallDetection"
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        Log.d(TAG, "Phone state: $state, number: $phoneNumber")

        if (state == TelephonyManager.EXTRA_STATE_RINGING && state != lastState) {
            val number = phoneNumber ?: "Unknown"
            Log.d(TAG, "Incoming call detected from: $number")

            // Check if AI agent is enabled
            val prefs = context.getSharedPreferences(AppConfig.PREFS_NAME, Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean(AppConfig.PREF_ENABLED, false)

            if (isEnabled) {
                // Start the call handler service
                val serviceIntent = Intent(context, CallHandlerService::class.java).apply {
                    putExtra("phone_number", number)
                    action = "ACTION_INCOMING_CALL"
                }
                context.startForegroundService(serviceIntent)
            }
        }

        lastState = state ?: TelephonyManager.EXTRA_STATE_IDLE
    }
}
