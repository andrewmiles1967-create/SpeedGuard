package uk.co.speedguard.app

import android.app.*
import android.content.*
import android.location.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import kotlin.math.roundToInt

class DriveService : Service(), LocationListener {
    companion object {
        const val ACTION_UPDATE = "uk.co.speedguard.app.UPDATE"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_LIMIT = "limit"
        const val EXTRA_ACTIVE = "active"
        private const val CHANNEL = "speedguard_drive"
    }

    private lateinit var lm: LocationManager
    private val provider: SpeedLimitProvider = DtroSpeedLimitProvider()
    private var lastWarn = 0L

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(7, Notification.Builder(this, CHANNEL)
            .setContentTitle("SpeedGuard monitoring")
            .setContentText("GPS speed monitoring is active")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true).build())
        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        try { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500L, 0f, this) } catch (_: SecurityException) { stopSelf() }
    }

    override fun onLocationChanged(location: Location) {
        val mph = if (location.hasSpeed()) (location.speed * 2.236936).roundToInt().coerceAtLeast(0) else 0
        val limit = provider.limitFor(location)
        if (limit != null && mph > limit && System.currentTimeMillis() - lastWarn > 5000) {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80).startTone(ToneGenerator.TONE_PROP_BEEP, 350)
            lastWarn = System.currentTimeMillis()
        }
        sendBroadcast(Intent(ACTION_UPDATE).setPackage(packageName)
            .putExtra(EXTRA_SPEED, mph).putExtra(EXTRA_LIMIT, limit ?: -1).putExtra(EXTRA_ACTIVE, true))
    }

    override fun onBind(intent: Intent?) = null
    override fun onDestroy() { if (::lm.isInitialized) lm.removeUpdates(this); super.onDestroy() }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Driving monitor", NotificationManager.IMPORTANCE_LOW))
        }
    }
}
