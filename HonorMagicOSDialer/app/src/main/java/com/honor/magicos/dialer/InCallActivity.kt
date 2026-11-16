package com.honor.magicos.dialer

import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color

class InCallActivity : AppCompatActivity() {

    private lateinit var statusView: TextView
    private lateinit var numberView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0B0D12"))
            setPadding(64, 96, 64, 96)
        }

        val badge = TextView(this).apply {
            text = "Honor MagicOS InCallService"
            textSize = 13f
            setTextColor(Color.parseColor("#34D399"))
            gravity = Gravity.CENTER
        }

        numberView = TextView(this).apply {
            val handle = CallManager.currentCall?.details?.handle?.schemeSpecificPart ?: "Unknown Caller"
            text = handle
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 12)
        }

        statusView = TextView(this).apply {
            text = "Connecting..."
            textSize = 15f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 96)
        }

        val actionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val answerBtn = Button(this).apply {
            text = "Answer"
            setBackgroundColor(Color.parseColor("#00B853"))
            setTextColor(Color.WHITE)
            setOnClickListener { CallManager.answer() }
        }

        val endBtn = Button(this).apply {
            text = "End Call"
            setBackgroundColor(Color.parseColor("#E84026"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                CallManager.hangup()
                finish()
            }
        }

        actionsRow.addView(answerBtn)
        actionsRow.addView(endBtn)

        root.addView(badge)
        root.addView(numberView)
        root.addView(statusView)
        root.addView(actionsRow)
        setContentView(root)

        CallManager.stateListener = { _, state ->
            runOnUiThread {
                when (state) {
                    Call.STATE_RINGING -> statusView.text = "Incoming Call..."
                    Call.STATE_DIALING -> statusView.text = "Dialing..."
                    Call.STATE_ACTIVE -> statusView.text = "Connected (HD Voice)"
                    Call.STATE_HOLDING -> statusView.text = "On Hold"
                    Call.STATE_DISCONNECTED -> {
                        statusView.text = "Call Ended"
                        finish()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        CallManager.stateListener = null
        super.onDestroy()
    }
}
