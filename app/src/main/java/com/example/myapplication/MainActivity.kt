/*
  File: app/src/main/java/com/example/myapplication/MainActivity.kt

  This is the simplified version of the MainActivity. It removes all logic
  related to the settings page and SharedPreferences.
*/
package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.view.OpenGlView
import com.pedro.rtmp.utils.ConnectCheckerRtmp

class MainActivity : AppCompatActivity(), ConnectCheckerRtmp, SurfaceHolder.Callback {

    private var rtmpCamera1: RtmpCamera1? = null
    private lateinit var startStopButton: Button
    private lateinit var rtmpUrlEditText: EditText
    private lateinit var openGlView: OpenGlView
    private lateinit var statusTextView: TextView

    private val requiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val PERMISSION_REQUEST_CODE = 101

    private var isCameraInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        // Initialize views
        startStopButton = findViewById(R.id.start_stop_button)
        rtmpUrlEditText = findViewById(R.id.rtmp_url)
        openGlView = findViewById(R.id.openGlView)
        statusTextView = findViewById(R.id.bitrate_text)

        openGlView.holder.addCallback(this)

        startStopButton.setOnClickListener {
            handleStreamButtonClick()
        }

        if (!hasAllPermissions()) {
            requestPermissions()
        }
    }

    private fun initializeCamera() {
        if (isCameraInitialized) return

        rtmpCamera1 = RtmpCamera1(openGlView, this)
        rtmpCamera1?.setReTries(10)
        isCameraInitialized = true
    }

    private fun handleStreamButtonClick() {
        val rtmpUrl = rtmpUrlEditText.text.toString().trim()
        if (rtmpUrl.isEmpty()) {
            Toast.makeText(this, "Please enter a valid RTMP URL", Toast.LENGTH_SHORT).show()
            return
        }

        if (rtmpCamera1 == null) {
            Toast.makeText(this, "Camera not ready.", Toast.LENGTH_SHORT).show()
            return
        }

        if (startStopButton.text.toString().equals("Start Stream", ignoreCase = true)) {
            if (rtmpCamera1?.isStreaming == false) {
                if (rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
                    startStopButton.text = "Stop Stream"
                    startStream(rtmpUrl)
                } else {
                    Toast.makeText(this, "Error preparing stream.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            startStopButton.text = "Start Stream"
            stopStream()
        }
    }

    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions not granted. The app cannot function.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startStream(rtmpUrl: String) {
        rtmpCamera1?.let {
            if (!it.isStreaming) {
                statusTextView.text = "Connecting..."
                it.startStream(rtmpUrl)
            }
        }
    }

    private fun stopStream() {
        rtmpCamera1?.let {
            if (it.isStreaming) {
                it.stopStream()
            }
        }
        statusTextView.text = "Stream stopped"
    }

    override fun onPause() {
        super.onPause()
        rtmpCamera1?.let {
            if (it.isStreaming) {
                it.stopStream()
                startStopButton.text = "Start Stream"
            }
            if (it.isOnPreview) {
                it.stopPreview()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (hasAllPermissions()) {
            initializeCamera()
            rtmpCamera1?.startPreview()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        rtmpCamera1?.let {
            if (it.isStreaming) it.stopStream()
            if (it.isOnPreview) it.stopPreview()
        }
        isCameraInitialized = false
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
        runOnUiThread {
            statusTextView.text = "Connection Started"
        }
    }

    override fun onConnectionSuccessRtmp() {
        runOnUiThread {
            statusTextView.text = "Connection Success"
            Toast.makeText(this@MainActivity, "Connection success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            statusTextView.text = "Connection Failed"
            Toast.makeText(this@MainActivity, "Connection failed: $reason", Toast.LENGTH_LONG).show()
            stopStream()
            startStopButton.text = "Start Stream"
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        runOnUiThread {
            val bitrateKbps = bitrate / 1024
            statusTextView.text = "$bitrateKbps Kbps"
        }
    }

    override fun onDisconnectRtmp() {
        runOnUiThread {
            statusTextView.text = "Disconnected"
            Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread {
            statusTextView.text = "Auth Error"
            Toast.makeText(this@MainActivity, "Auth error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread {
            statusTextView.text = "Auth Success"
            Toast.makeText(this@MainActivity, "Auth success", Toast.LENGTH_SHORT).show()
        }
    }
}
