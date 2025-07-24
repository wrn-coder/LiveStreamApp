/*
  File: app/src/main/java/com/example/myapplication/SettingsActivity.kt

  This activity allows the user to set and save the RTMP URL.
  It uses SharedPreferences for persistent storage.
*/
package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var rtmpUrlEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREFS_NAME = "StreamSettings"
        const val KEY_RTMP_URL = "rtmp_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize views
        rtmpUrlEditText = findViewById(R.id.rtmp_url_edittext)
        saveButton = findViewById(R.id.save_button)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load the saved URL and display it
        loadUrl()

        saveButton.setOnClickListener {
            saveUrl()
        }
    }

    private fun loadUrl() {
        val savedUrl = sharedPreferences.getString(KEY_RTMP_URL, "")
        rtmpUrlEditText.setText(savedUrl)
    }

    private fun saveUrl() {
        val url = rtmpUrlEditText.text.toString().trim()
        if (url.isNotEmpty()) {
            sharedPreferences.edit().putString(KEY_RTMP_URL, url).apply()
            Toast.makeText(this, "URL saved!", Toast.LENGTH_SHORT).show()
            finish() // Close the settings activity and return to the main screen
        } else {
            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
        }
    }
}
