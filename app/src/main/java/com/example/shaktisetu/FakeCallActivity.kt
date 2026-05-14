package com.example.shaktisetu

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class FakeCallActivity : AppCompatActivity() {

    private var selectedDelay: Long = 5000

    private var callerName = "Harsh"

    private lateinit var timerButtons:
            List<TextView>

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_fake_call
        )

        BottomNavHelper.setup(
            this,
            "call"
        )

        val btnBack =
            findViewById<ImageView>(
                R.id.btnBack
            )

        val tvCallerAvatar =
            findViewById<TextView>(
                R.id.tvCallerAvatar
            )

        val tvCallerName =
            findViewById<TextView>(
                R.id.tvCallerName
            )

        val btnEditCaller =
            findViewById<LinearLayout>(
                R.id.btnEditCaller
            )

        val btn5s =
            findViewById<TextView>(
                R.id.btn5s
            )

        val btn10s =
            findViewById<TextView>(
                R.id.btn10s
            )

        val btn30s =
            findViewById<TextView>(
                R.id.btn30s
            )

        val btn60s =
            findViewById<TextView>(
                R.id.btn60s
            )

        val ringtoneRow =
            findViewById<LinearLayout>(
                R.id.ringtoneRow
            )

        val btnScheduleCall =
            findViewById<Button>(
                R.id.btnScheduleCall
            )

        timerButtons = listOf(
            btn5s,
            btn10s,
            btn30s,
            btn60s
        )

        val timerValues = listOf(
            5000L,
            10000L,
            30000L,
            60000L
        )

        // Default Selection
        updateSelectedTimer(btn5s)

        // Back
        btnBack.setOnClickListener {
            finish()
        }

        // Edit Caller
        btnEditCaller.setOnClickListener {

            showCallerNameDialog(
                tvCallerName,
                tvCallerAvatar
            )
        }

        // Timer Selection
        timerButtons.forEachIndexed {
                index,
                button ->

            button.setOnClickListener {

                selectedDelay =
                    timerValues[index]

                updateSelectedTimer(button)
            }
        }

        // Ringtone
        ringtoneRow.setOnClickListener {

            Toast.makeText(
                this,
                "🎵 Custom ringtone coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Schedule Call
        btnScheduleCall.setOnClickListener {

            Toast.makeText(
                this,
                "📞 Fake call scheduled",
                Toast.LENGTH_SHORT
            ).show()

            Handler(
                Looper.getMainLooper()
            ).postDelayed({

                openIncomingCall()

            }, selectedDelay)
        }
    }

    // Caller Dialog
    private fun showCallerNameDialog(
        tvCallerName: TextView,
        tvCallerAvatar: TextView
    ) {

        val editText = EditText(this)

        editText.setText(callerName)

        editText.hint = "Enter caller name"

        AlertDialog.Builder(this)

            .setTitle("Edit Caller")

            .setView(editText)

            .setPositiveButton("Save") {
                    _,
                    _ ->

                val newName =
                    editText.text
                        .toString()
                        .trim()

                if (newName.isNotEmpty()) {

                    callerName = newName

                    tvCallerName.text =
                        callerName

                    tvCallerAvatar.text =
                        callerName.first()
                            .uppercase()
                }
            }

            .setNegativeButton(
                "Cancel",
                null
            )

            .show()
    }

    // Update Timer UI
    private fun updateSelectedTimer(
        selectedButton: TextView
    ) {

        timerButtons.forEach { button ->

            button.setBackgroundResource(
                R.drawable
                    .timer_btn_unselected
            )

            button.setTextColor(

                ContextCompat.getColor(
                    this,
                    R.color.timer_unselected_text
                )
            )
        }

        selectedButton.setBackgroundResource(
            R.drawable.timer_btn_selected
        )

        selectedButton.setTextColor(

            ContextCompat.getColor(
                this,
                android.R.color.white
            )
        )
    }

    // Open Incoming Call
    private fun openIncomingCall() {

        val intent = Intent(
            this,
            IncomingCallActivity::class.java
        )

        intent.putExtra(
            "caller_name",
            callerName
        )

        val options =
            ActivityOptions
                .makeCustomAnimation(
                    this,
                    R.anim.zoom_fade_in,
                    R.anim.zoom_fade_out
                )

        startActivity(
            intent,
            options.toBundle()
        )
    }

    override fun finish() {

        super.finish()

        overridePendingTransition(
            R.anim.zoom_fade_in_back,
            R.anim.zoom_fade_out_back
        )
    }
}