package com.viciouslayer.utility

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnStop = findViewById<Button>(R.id.btn_stop)
        val btnPushNotif = findViewById<Button>(R.id.btn_send_push)
        val txtInput = findViewById<EditText>(R.id.overlay_label_input)

        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                val serviceIntent = Intent(this, OverlayService::class.java).apply {
                    putExtra("EXTRA_TEXT", txtInput.text.toString())
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
        }

        btnPushNotif.setOnClickListener {
            sendNotification("ViciousLayer Update", "Dynamic notification payload triggered globally.")
        }

        requestNotificationPermission()
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "user_notifications"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alert Alerts", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (!isGranted) Toast.makeText(this, "Alert push permissions denied", Toast.LENGTH_SHORT).show()
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
