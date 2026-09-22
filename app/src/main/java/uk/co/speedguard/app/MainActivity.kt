package uk.co.speedguard.app

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.widget.*

class MainActivity : Activity() {
    private lateinit var speed: TextView
    private lateinit var limit: TextView
    private lateinit var status: TextView
    private lateinit var button: Button
    private var active = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val mph = intent?.getIntExtra(DriveService.EXTRA_SPEED, 0) ?: 0
            val lim = intent?.getIntExtra(DriveService.EXTRA_LIMIT, -1) ?: -1
            speed.text = mph.toString()
            limit.text = if (lim > 0) lim.toString() else "--"
            status.text = if (lim > 0) "MONITORING" else "GPS ACTIVE · LIMIT DATA PENDING"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        speed = findViewById(R.id.speed); limit = findViewById(R.id.limit); status = findViewById(R.id.status); button = findViewById(R.id.startStop)
        button.setOnClickListener { if (active) stopDrive() else requestAndStart() }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(DriveService.ACTION_UPDATE)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED) else @Suppress("DEPRECATION") registerReceiver(receiver, filter)
    }
    override fun onStop() { unregisterReceiver(receiver); super.onStop() }

    private fun requestAndStart() {
        val needed = mutableListOf<String>()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) needed += Manifest.permission.ACCESS_FINE_LOCATION
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) needed += Manifest.permission.POST_NOTIFICATIONS
        if (needed.isNotEmpty()) requestPermissions(needed.toTypedArray(), 42) else startDrive()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) startDrive()
        else status.text = "LOCATION PERMISSION REQUIRED"
    }
    private fun startDrive() {
        startForegroundService(Intent(this, DriveService::class.java)); active = true; button.text = "STOP DRIVE"; status.text = "ACQUIRING GPS…"
    }
    private fun stopDrive() {
        stopService(Intent(this, DriveService::class.java)); active = false; button.text = "START DRIVE"; status.text = "READY"; speed.text = "0"; limit.text = "--"
    }
}
