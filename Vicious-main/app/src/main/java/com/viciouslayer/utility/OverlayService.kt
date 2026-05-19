package com.viciouslayer.utility

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var webView: WebView? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val displayText = intent?.getStringExtra("EXTRA_TEXT") ?: "ViciousLayer"
        val opacity = intent?.getIntExtra("EXTRA_OPACITY", 85) ?: 85
        val colorHex = intent?.getStringExtra("EXTRA_COLOR") ?: "#E03060"
        val passthrough = intent?.getBooleanExtra("EXTRA_PASSTHROUGH", true) ?: true

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ViciousLayer Interface Service")
            .setContentText("HTML System Node Overlay Active.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (overlayView == null) {
            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
            }

            // FLAG_NOT_FOCUSABLE is always set so the overlay never steals IME focus.
            // FLAG_NOT_TOUCH_MODAL is added when passthrough is OFF so the view can
            // receive its own touch events while still not blocking the rest of the screen.
            // When passthrough is ON both flags are set, making touches fall through entirely.
            var flagsMask = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            if (!passthrough) {
                flagsMask = flagsMask or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                flagsMask,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 200
            }

            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            overlayView = inflater.inflate(R.layout.overlay_layout, null)

            webView = overlayView?.findViewById(R.id.overlay_webview)
            webView?.apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                setBackgroundColor(0)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        executeJsUpdates(displayText, opacity, colorHex)
                    }
                }
                loadUrl("file:///android_asset/dashboard.html")
            }

            // Motion Tracking Matrix logic
            overlayView?.setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(overlayView, params)
                            return true
                        }
                    }
                    return false
                }
            })

            windowManager.addView(overlayView, params)
        } else {
            executeJsUpdates(displayText, opacity, colorHex)
        }

        return START_STICKY
    }

    private fun executeJsUpdates(text: String, opacity: Int, color: String) {
        // Sanitise text to avoid breaking the JS string literal
        val safeText = text.replace("'", "\\'").replace("\n", "\\n")
        webView?.evaluateJavascript("if(typeof updateOverlayText === 'function') updateOverlayText('$safeText');", null)
        webView?.evaluateJavascript("document.body.style.borderColor = '$color';", null)
        webView?.evaluateJavascript("document.body.style.background = 'rgba(10,10,15, ${opacity / 100.0})';", null)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Hardware Node Engine", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager.removeView(it) }
        webView?.destroy()
        overlayView = null
        webView = null
    }

    companion object {
        const val CHANNEL_ID = "vicious_web_overlay_core"
        const val NOTIFICATION_ID = 9912
    }
}
