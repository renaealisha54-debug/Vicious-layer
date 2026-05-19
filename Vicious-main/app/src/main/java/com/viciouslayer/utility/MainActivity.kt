package com.viciouslayer.utility

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var txtLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Binding UI Nodes
        val btnOverlayPerm = findViewById<Button>(R.id.btn_perm_overlay)
        val btnNotifPerm   = findViewById<Button>(R.id.btn_perm_notif)
        val btnStart       = findViewById<Button>(R.id.btn_start)
        val btnStop        = findViewById<Button>(R.id.btn_stop)
        val statusDot      = findViewById<View>(R.id.status_dot)
        val lblStatus      = findViewById<TextView>(R.id.lbl_service_status)
        val lblDetail      = findViewById<TextView>(R.id.lbl_service_detail)
        val txtInput       = findViewById<EditText>(R.id.overlay_label_input)
        val txtColor       = findViewById<EditText>(R.id.overlay_color_input)
        val sliderOpacity  = findViewById<SeekBar>(R.id.slider_opacity)
        val chkPassthrough = findViewById<CheckBox>(R.id.chk_passthrough)
        txtLog = findViewById(R.id.txt_system_log)

        // Enable scrolling on the fixed-height log TextView
        txtLog.movementMethod = ScrollingMovementMethod()

        // Check and update permission button states dynamically
        updatePermissionButtonStates(btnOverlayPerm, btnNotifPerm)

        btnOverlayPerm.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            appendLog("Dispatched SYSTEM_ALERT_WINDOW settings intent.", "INFO")
        }

        btnNotifPerm.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                appendLog("POST_NOTIFICATIONS implicit grant on pre-API-33 device.", "INFO")
            }
        }

        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission is mandatory!", Toast.LENGTH_SHORT).show()
                appendLog("Engine exception: Window permission gate locked.", "ERROR")
                return@setOnClickListener
            }

            val serviceIntent = Intent(this, OverlayService::class.java).apply {
                putExtra("EXTRA_TEXT",        txtInput.text.toString())
                putExtra("EXTRA_OPACITY",     sliderOpacity.progress)
                putExtra("EXTRA_COLOR",       txtColor.text.toString())
                putExtra("EXTRA_PASSTHROUGH", chkPassthrough.isChecked)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            statusDot.setBackgroundColor(Color.parseColor("#30E088"))
            lblStatus.text = "OverlayService · RUNNING"
            lblDetail.text = "Sandbox initialised inside active WindowManager layer."
            btnStart.isEnabled = false
            btnStop.isEnabled  = true
            appendLog("Foreground service started.", "SUCCESS")
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            statusDot.setBackgroundColor(Color.parseColor("#666680"))
            lblStatus.text = "OverlayService · STOPPED"
            lblDetail.text = "Ready to launch"
            btnStart.isEnabled = true
            btnStop.isEnabled  = false
            appendLog("Service stopped by user.", "WARN")
        }
    }

    private val requestNotifLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) appendLog("POST_NOTIFICATIONS granted successfully.", "SUCCESS")
            else           appendLog("POST_NOTIFICATIONS denied. Service tracking restricted.", "ERROR")
        }

    private fun updatePermissionButtonStates(overlayBtn: Button, notifBtn: Button) {
        if (Settings.canDrawOverlays(this)) {
            overlayBtn.text = "OVERLAY ACCESS: GRANTED"
            overlayBtn.isEnabled = false
            overlayBtn.setBackgroundColor(Color.parseColor("#30E088"))
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            notifBtn.text = "NOTIFICATION ACCESS: GRANTED"
            notifBtn.isEnabled = false
            notifBtn.setBackgroundColor(Color.parseColor("#30E088"))
        }
    }

    private fun appendLog(msg: String, level: String) {
        val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        txtLog.append("\n[$timeStamp] [$level] $msg")
        // Scroll to the bottom after appending
        val scrollAmount = txtLog.layout?.getLineTop(txtLog.lineCount) ?: 0
        val scrollDelta  = scrollAmount - txtLog.height
        if (scrollDelta > 0) txtLog.scrollTo(0, scrollDelta)
    }

    override fun onResume() {
        super.onResume()
        updatePermissionButtonStates(
            findViewById(R.id.btn_perm_overlay),
            findViewById(R.id.btn_perm_notif)
        )
    }
}
