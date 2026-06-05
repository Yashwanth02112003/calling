package com.aicallagent.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallDetectionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallDetection"
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
        // Track if we're already handling a call (AI bridge active)
        var isBridgingActive = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        Log.d(TAG, "Phone state: $state, number: $phoneNumber, bridging: $isBridgingActive")

        if (state == TelephonyManager.EXTRA_STATE_RINGING && state != lastState) {
            val number = phoneNumber ?: "Unknown"

            val prefs = context.getSharedPreferences(AppConfig.PREFS_NAME, Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean(AppConfig.PREF_ENABLED, false)

            if (isEnabled && !isBridgingActive) {
                // First incoming call - start the AI bridge
                Log.d(TAG, "First incoming call from: $number - starting AI bridge")
                isBridgingActive = true

                val serviceIntent = Intent(context, CallHandlerService::class.java).apply {
                    putExtra("phone_number", number)
                    action = "ACTION_INCOMING_CALL"
                }
                context.startForegroundService(serviceIntent)
            }
            // If isBridgingActive is true, this is the AI calling back
            // Android will auto-answer via call waiting / the user picks up
        }

        if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            // Call ended - reset bridging state
            if (isBridgingActive) {
                isBridgingActive = false
                Log.d(TAG, "Call ended, bridging reset")
            }
        }

        lastState = state ?: TelephonyManager.EXTRA_STATE_IDLE
    }
}
