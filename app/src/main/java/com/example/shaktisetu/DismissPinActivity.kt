package com.example.shaktisetu

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class DismissPinActivity : AppCompatActivity() {

    private var pin = ""

    private val MAX_PIN_LENGTH = 4

    private lateinit var tvPinDisplay:
            TextView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_dismiss_pin
        )

        tvPinDisplay =
            findViewById(R.id.tvPinDisplay)

        val btnConfirm =
            findViewById<Button>(
                R.id.btnConfirm
            )

        val btnBackspace =
            findViewById<TextView>(
                R.id.btnBackspace
            )

        val numberButtons = listOf(

            findViewById<TextView>(R.id.btn0),
            findViewById<TextView>(R.id.btn1),
            findViewById<TextView>(R.id.btn2),
            findViewById<TextView>(R.id.btn3),
            findViewById<TextView>(R.id.btn4),
            findViewById<TextView>(R.id.btn5),
            findViewById<TextView>(R.id.btn6),
            findViewById<TextView>(R.id.btn7),
            findViewById<TextView>(R.id.btn8),
            findViewById<TextView>(R.id.btn9)
        )

        // Number Buttons
        numberButtons.forEachIndexed {
                index,
                button ->

            button.setOnClickListener {

                if (
                    pin.length <
                    MAX_PIN_LENGTH
                ) {

                    pin += index.toString()

                    updatePinDisplay()
                }
            }
        }

        // Backspace
        btnBackspace.setOnClickListener {

            if (pin.isNotEmpty()) {

                pin = pin.dropLast(1)

                updatePinDisplay()
            }
        }

        // Confirm
        btnConfirm.setOnClickListener {

            if (
                pin.length <
                MAX_PIN_LENGTH
            ) {

                Toast.makeText(
                    this,
                    "Enter 4-digit PIN!",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            verifyPin()
        }

        // Block Back Press
        onBackPressedDispatcher.addCallback(
            this,

            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    Toast.makeText(
                        this@DismissPinActivity,
                        "🔒 Enter PIN to dismiss SOS!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    // Update PIN UI
    private fun updatePinDisplay() {

        tvPinDisplay.text =
            "●".repeat(pin.length)
    }

    // Verify PIN
    private fun verifyPin() {

        val sharedPref =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        val savedPin =
            sharedPref.getString(
                "user_pin",
                ""
            ) ?: ""

        if (pin == savedPin) {

            Toast.makeText(
                this,
                "✅ SOS Dismissed",
                Toast.LENGTH_SHORT
            ).show()

            setResult(RESULT_OK)

            finish()

        } else {

            Toast.makeText(
                this,
                "❌ Wrong PIN! SOS continues!",
                Toast.LENGTH_LONG
            ).show()

            pin = ""

            updatePinDisplay()
        }
    }
}