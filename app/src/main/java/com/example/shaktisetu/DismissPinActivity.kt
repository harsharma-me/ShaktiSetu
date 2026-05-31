package com.example.shaktisetu

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.shaktisetu.ui.screens.DismissPinScreen
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme

class DismissPinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShaktiSetuTheme {
                DismissPinScreen(
                    onConfirm = { pin ->
                        verifyPin(pin)
                    }
                )
            }
        }

        // Block Back Press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@DismissPinActivity, "🔒 Enter PIN to dismiss SOS!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun verifyPin(pin: String) {
        val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        val savedPin = sharedPref.getString("user_pin", "") ?: ""

        if (pin == savedPin) {
            Toast.makeText(this, "✅ SOS Dismissed", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "❌ Wrong PIN! SOS continues!", Toast.LENGTH_LONG).show()
        }
    }
}
