package com.valeno.shaktisetu

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.valeno.shaktisetu.ui.screens.EditProfileScreen
import com.valeno.shaktisetu.ui.theme.ShaktiSetuTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private var isUpdating by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        val initialName = sharedPreferences.getString("user_name", "User") ?: "User"
        val initialEmail = sharedPreferences.getString("user_email", "user@example.com") ?: "user@example.com"
        val initialPhone = sharedPreferences.getString("user_phone", "") ?: ""
        val initialAddress = sharedPreferences.getString("user_address", "") ?: ""

        setContent {
            ShaktiSetuTheme {
                EditProfileScreen(
                    initialName = initialName,
                    initialEmail = initialEmail,
                    initialPhone = initialPhone,
                    initialAddress = initialAddress,
                    isUpdating = isUpdating,
                    onBackClick = { finish() },
                    onUpdateClick = { name, phone, address ->
                        updateProfile(name, phone, address)
                    }
                )
            }
        }
    }

    private fun updateProfile(name: String, phone: String, address: String) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid ?: return

        isUpdating = true

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "address" to address
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE).edit()
                            .putString("user_name", name)
                            .putString("user_phone", phone)
                            .putString("user_address", address)
                            .apply()
                    }

                    Toast.makeText(this@EditProfileActivity, "✅ Profile Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                isUpdating = false
            }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.zoom_fade_in_back,
                R.anim.zoom_fade_out_back
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(
                R.anim.zoom_fade_in_back,
                R.anim.zoom_fade_out_back
            )
        }
    }
}
