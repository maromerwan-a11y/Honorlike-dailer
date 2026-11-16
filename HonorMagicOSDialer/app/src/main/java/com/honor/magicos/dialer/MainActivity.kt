package com.honor.magicos.dialer

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val isDefault = isCurrentlyDefaultDialer()
        webView.evaluateJavascript("window.onDefaultDialerRoleChanged && window.onDefaultDialerRoleChanged($isDefault);", null)
        if (isDefault) {
            requestRuntimePermissions()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val isDef = isCurrentlyDefaultDialer()
                    evaluateJavascript("window.onDefaultDialerRoleChanged && window.onDefaultDialerRoleChanged($isDef);", null)
                }
            }
            addJavascriptInterface(AndroidDialerBridge(), "AndroidDialerBridge")
        }

        setContentView(webView)
        webView.loadUrl("file:///android_asset/honor_dialer.html")

        // Prompt to become Default Phone App on first launch if not already set
        if (!isCurrentlyDefaultDialer()) {
            promptForDefaultDialerRole()
        } else {
            requestRuntimePermissions()
        }
    }

    private fun isCurrentlyDefaultDialer(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(Context.ROLE_SERVICE) as RoleManager
            rm.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            val tm = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            packageName == tm.defaultDialerPackage
        }
    }

    fun promptForDefaultDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                roleLauncher.launch(intent)
            }
        } else {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (packageName != telecomManager.defaultDialerPackage) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                }
                roleLauncher.launch(intent)
            }
        }
    }

    private fun requestRuntimePermissions() {
        val perms = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1001)
        }
    }

    inner class AndroidDialerBridge {

        @JavascriptInterface
        fun requestDefaultDialerRole() {
            runOnUiThread {
                promptForDefaultDialerRole()
            }
        }

        @JavascriptInterface
        fun isDefaultDialer(): Boolean {
            return isCurrentlyDefaultDialer()
        }

        @JavascriptInterface
        fun placeCall(number: String, simSlot: Int) {
            runOnUiThread {
                val uri = Uri.fromParts("tel", number, null)
                val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    val extras = Bundle()
                    try {
                        val accounts = telecomManager.callCapablePhoneAccounts
                        val index = (simSlot - 1).coerceIn(0, (accounts.size - 1).coerceAtLeast(0))
                        if (accounts.isNotEmpty()) {
                            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accounts[index])
                        }
                    } catch (_: SecurityException) {}
                    telecomManager.placeCall(uri, extras)
                } else {
                    val dialIntent = Intent(Intent.ACTION_DIAL, uri)
                    startActivity(dialIntent)
                    Toast.makeText(this@MainActivity, "Grant Phone permission to place direct Dual-SIM calls", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
