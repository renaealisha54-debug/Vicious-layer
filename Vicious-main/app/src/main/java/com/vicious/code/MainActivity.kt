private val OVERLAY_PERMISSION_REQUEST = 1001

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val btnLaunch = findViewById<Button>(R.id.btnLaunch)
    val btnStop = findViewById<Button>(R.id.btnStop)

    btnLaunch.setOnClickListener {
        if (canDrawOverlays()) {
            startOverlayService()
        } else {
            requestOverlayPermission()
        }
    }

    btnStop.setOnClickListener {
        stopService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Overlay stopped", Toast.LENGTH_SHORT).show()
    }
}

private fun canDrawOverlays(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(this)
    } else {
        true
    }
}

private fun requestOverlayPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
    }
}

private fun startOverlayService() {
    val intent = Intent(this, OverlayService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
    Toast.makeText(this, "Overlay started", Toast.LENGTH_SHORT).show()
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == OVERLAY_PERMISSION_REQUEST) {
        if (canDrawOverlays()) {
            startOverlayService()
        } else {
            Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_LONG).show()
        }
    }
}
