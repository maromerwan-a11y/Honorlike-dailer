package com.honor.magicos.dialer

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService

class HonorInCallService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        CallManager.inCallService = this
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.attachCall(call)
        val intent = Intent(this, InCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.clearCall(call)
    }

    override fun onDestroy() {
        if (CallManager.inCallService == this) {
            CallManager.inCallService = null
        }
        super.onDestroy()
    }
}
