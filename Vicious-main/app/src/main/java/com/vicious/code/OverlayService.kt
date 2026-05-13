private lateinit var windowManager: WindowManager
private var overlayView: android.view.View? = null
private val CHANNEL_ID = "vicious_overlay_channel"
private val NOTIFICATION_ID = 1

override fun onBind(intent: Intent?): IBinder? = null

override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    startForeground(NOTIFICATION_ID, buildNotification())
    showOverlay()
}

private fun showOverlay() {
    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

    val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE
    }

    val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        layoutFlag,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )
    params.gravity = Gravity.TOP or Gravity.START
    params.x = 0
    params.y = 100

    overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
    windowManager.addView(overlayView, params)
}

private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ViciousLayer Overlay",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

private fun buildNotification(): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("ViciousLayer Active")
        .setContentText("System overlay is running")
        .setSmallIcon(android.R.drawable.ic_menu_view)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
}

override fun onDestroy() {
    super.onDestroy()
    overlayView?.let { windowManager.removeView(it) }
}
