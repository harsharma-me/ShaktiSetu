package com.example.shaktisetu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreatePinActivity : AppCompatActivity() {

    private var pin = ""

    private val MAX_PIN_LENGTH = 4

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_create_pin
        )

        val tvPinDisplay =
            findViewById<TextView>(
                R.id.tvPinDisplay
            )

        val btnConfirm =
            findViewById<Button>(
                R.id.btnConfirm
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

        val btnBackspace =
            findViewById<TextView>(
                R.id.btnBackspace
            )

        // Number Clicks
        numberButtons.forEachIndexed {
                index,
                button ->

            button.setOnClickListener {

                if (
                    pin.length <
                    MAX_PIN_LENGTH
                ) {

                    pin += index.toString()

                    tvPinDisplay.text =
                        "●".repeat(pin.length)
                }
            }
        }

        // Backspace
        btnBackspace.setOnClickListener {

            if (pin.isNotEmpty()) {

                pin = pin.dropLast(1)

                tvPinDisplay.text =
                    "●".repeat(pin.length)
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
                    "Please enter 4 digit PIN!",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            savePinLocally(pin)
        }
    }

    // Save PIN
    private fun savePinLocally(
        pin: String
    ) {

        try {

            val sharedPref =
                getSharedPreferences(
                    "ShaktiSetuPrefs",
                    MODE_PRIVATE
                )

            sharedPref.edit()

                .putString(
                    "user_pin",
                    pin
                )

                .apply()

            Toast.makeText(
                this,
                "✅ PIN Created!",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                this,
                "❌ Failed to save PIN!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}