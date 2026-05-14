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

class IncomingCallActivity : AppCompatActivity() {

    private var mediaPlayer:
            MediaPlayer? = null

    private var vibrator:
            Vibrator? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_incoming_call
        )

        val callerName =
            intent.getStringExtra(
                "caller_name"
            ) ?: "Unknown"

        val tvIncomingAvatar =
            findViewById<TextView>(
                R.id.tvIncomingAvatar
            )

        val tvIncomingName =
            findViewById<TextView>(
                R.id.tvIncomingName
            )

        val tvIncomingNumber =
            findViewById<TextView>(
                R.id.tvIncomingNumber
            )

        val btnDecline =
            findViewById<TextView>(
                R.id.btnDecline
            )

        val btnAccept =
            findViewById<LinearLayout>(
                R.id.btnAccept
            )

        val btnAnswer =
            findViewById<TextView>(
                R.id.btnAnswer
            )

        val btnMessage =
            findViewById<LinearLayout>(
                R.id.btnMessage
            )

        // Caller Info
        tvIncomingName.text =
            callerName

        tvIncomingAvatar.text =
            callerName.first()
                .uppercase()

        tvIncomingNumber.text =
            "Mobile +91 82950 00000"

        // Start Effects
        startRingtone()

        startVibration()

        // Decline
        btnDecline.setOnClickListener {

            stopEverything()

            finish()
        }

        // Accept
        btnAccept.setOnClickListener {

            answerCall(callerName)
        }

        // Answer Text
        btnAnswer.setOnClickListener {

            answerCall(callerName)
        }

        // Message
        btnMessage.setOnClickListener {

            stopEverything()

            Toast.makeText(
                this,
                "💬 Message sent!",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }

        // Block Back Press
        onBackPressedDispatcher.addCallback(
            this,

            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    // Block Back
                }
            }
        )
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