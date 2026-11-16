package com.honor.magicos.dialer

import android.telecom.Call
import android.telecom.VideoProfile

object CallManager {
    var currentCall: Call? = null
    var inCallService: HonorInCallService? = null
    var stateListener: ((Call, Int) -> Unit)? = null

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            stateListener?.invoke(call, state)
        }
    }

    fun attachCall(call: Call) {
        currentCall?.unregisterCallback(callback)
        currentCall = call
        call.registerCallback(callback)
        stateListener?.invoke(call, call.details.state)
    }

    fun clearCall(call: Call) {
        call.unregisterCallback(callback)
        if (currentCall == call) {
            currentCall = null
        }
    }

    fun answer() {
        currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        currentCall?.disconnect()
    }

    fun setHold(hold: Boolean) {
        if (hold) currentCall?.hold() else currentCall?.unhold()
    }

    fun playDtmf(digit: Char) {
        currentCall?.playDtmfTone(digit)
        currentCall?.stopDtmfTone()
    }
}
