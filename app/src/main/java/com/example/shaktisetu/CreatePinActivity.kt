package com.example.shaktisetu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.shaktisetu.ui.screens.CreatePinScreen
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme
import com.google.firebase.firestore.FirebaseFirestore

class CreatePinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShaktiSetuTheme {
                CreatePinScreen(
                    onConfirm = { pin ->
                        savePinLocally(pin)
                    }
                )
            }
        }
    }

    private fun savePinLocally(pin: String) {
        try {
            val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
            val userUid = sharedPref.getString("user_uid", "") ?: ""

            sharedPref.edit()
                .putString("user_pin", pin)
                .apply()

            if (userUid.isNotEmpty()) {
                FirebaseFirestore.getInstance()
                    .collection("users").document(userUid)
                    .update("pin_set", true)
            }

            Toast.makeText(this, "✅ PIN Created!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "❌ Failed to save PIN!", Toast.LENGTH_SHORT).show()
        }
    }
}
