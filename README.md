# ViciousLayer

Android system overlay utility. Renders a draggable, transparent WebView layer on top of all apps via WindowManager.

## Features

- Foreground service with persistent notification
- Draggable overlay via touch tracking
- WebView loads local dashboard.html from assets
- Configurable label text, opacity, and accent color
- Touch passthrough toggle (FLAG_NOT_FOCUSABLE)
- Runtime permission handling for SYSTEM_ALERT_WINDOW and POST_NOTIFICATIONS

## Requirements

- Android 7.0+ (API 24)
- Target SDK 34
- SYSTEM_ALERT_WINDOW permission (granted manually via settings)
- POST_NOTIFICATIONS permission (API 33+)

## Package

com.viciouslayer.utility

## Build

Open in Android Studio and run, or:

./gradlew assembleDebug

## Permissions

SYSTEM_ALERT_WINDOW — required to draw over other apps
POST_NOTIFICATIONS — required for foreground service notification on API 33+
FOREGROUND_SERVICE — required to start foreground service
FOREGROUND_SERVICE_SPECIAL_USE — required for API 34+
