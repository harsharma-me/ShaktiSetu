package com.example.shaktisetu

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

import androidx.activity.compose.setContent
import com.example.shaktisetu.ui.screens.IncomingCallScreen
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme

class IncomingCallActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callerName = intent.getStringExtra("caller_name") ?: "Unknown"
        val phoneNumber = intent.getStringExtra("phone_number") ?: "Mobile +91 82950 00000"

        setContent {
            ShaktiSetuTheme {
                IncomingCallScreen(
                    callerName = callerName,
                    phoneNumber = phoneNumber,
                    onDecline = {
                        stopEverything()
                        finish()
                    },
                    onAccept = {
                        answerCall(callerName)
                    },
                    onMessage = {
                        stopEverything()
                        Toast.makeText(this, "💬 Message sent!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }

        // Start Effects
        startRingtone()
        startVibration()

        // Block Back Press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Block Back
            }
        })
    }

    // Start Ringtone
    private fun startRingtone() {

        try {

            val ringtoneUri =
                RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_RINGTONE
                )

            mediaPlayer =
                MediaPlayer.create(
                    this,
                    ringtoneUri
                )

            mediaPlayer?.apply {

                isLooping = true

                start()
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // Start Vibration
    private fun startVibration() {

        try {

            vibrator =
                getSystemService(
                    VIBRATOR_SERVICE
                ) as Vibrator

            val pattern =
                longArrayOf(
                    0,
                    1000,
                    500,
                    1000,
                    500
                )

            if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                vibrator?.vibrate(

                    VibrationEffect
                        .createWaveform(
                            pattern,
                            0
                        )
                )

            } else {

                @Suppress("DEPRECATION")

                vibrator?.vibrate(
                    pattern,
                    0
                )
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // Answer Call
    private fun answerCall(
        callerName: String
    ) {

        stopEverything()

        Toast.makeText(
            this,
            "📞 Connected with $callerName",
            Toast.LENGTH_LONG
        ).show()

        finish()
    }

    // Stop Everything
    private fun stopEverything() {

        try {

            mediaPlayer?.apply {

                if (isPlaying) {
                    stop()
                }

                release()
            }

            mediaPlayer = null

        } catch (e: Exception) {

            e.printStackTrace()
        }

        vibrator?.cancel()
    }

    override fun onDestroy() {

        super.onDestroy()

        stopEverything()
    }
}