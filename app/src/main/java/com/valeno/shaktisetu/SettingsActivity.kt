package com.valeno.shaktisetu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.valeno.shaktisetu.ui.screens.SettingsScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val profileProgress = mutableStateOf(0)
    private val shakeEnabled = mutableStateOf(true)
    private val voiceEnabled = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)

        loadSettings()

        setContent {
            com.valeno.shaktisetu.ui.theme.ShaktiSetuTheme {
                UpdateManager.CheckForUpdates(this)
                SettingsScreen(
                    profileProgress = profileProgress.value,
                    isShakeEnabled = shakeEnabled.value,
                    onShakeToggle = { enabled -> updateSetting("shake_detection", enabled) },
                    isVoiceEnabled = voiceEnabled.value,
                    onVoiceToggle = { enabled -> updateSetting("voice_detection", enabled) },
                    onActionClick = { action ->
                        when (action) {
                            "profile" -> startActivity(Intent(this, ProfileActivity::class.java))
                            "change_pin" -> showChangePinDialog()
                            "reset_password" -> showResetPasswordDialog()
                            "terms" -> showTermsDialog()
                            "logout" -> showLogoutDialog()
                        }
                    },
                    onTabClick = { tab ->
                        BottomNavHelper.handleTabClick(this, tab)
                    }
                )
            }
        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val progress = withContext(Dispatchers.Default) { calculateProfileProgress() }
            val shake = sharedPreferences.getBoolean("shake_detection", true)
            val voice = sharedPreferences.getBoolean("voice_detection", false)
            
            profileProgress.value = progress
            shakeEnabled.value = shake
            voiceEnabled.value = voice
        }
    }

    private fun updateSetting(key: String, value: Boolean) {
        lifecycleScope.launch {
            when (key) {
                "shake_detection" -> shakeEnabled.value = value
                "voice_detection" -> voiceEnabled.value = value
            }
            
            withContext(Dispatchers.IO) {
                sharedPreferences.edit().putBoolean(key, value).apply()
            }
        }
    }

    private fun calculateProfileProgress(): Int {
        val name = sharedPreferences.getString("user_name", "") ?: ""
        val email = sharedPreferences.getString("user_email", "") ?: ""
        val phone = sharedPreferences.getString("user_phone", "") ?: ""
        val address = sharedPreferences.getString("user_address", "") ?: ""
        var completed = 0
        if (name.isNotBlank()) completed++
        if (email.isNotBlank()) completed++
        if (phone.isNotBlank()) completed++
        if (address.isNotBlank()) completed++
        return (completed * 100) / 4
    }

    private fun showTermsDialog() {
        TermsConditionsDialog(this) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    sharedPreferences.edit().putBoolean("terms_agreed", true)
                        .putLong("terms_agreed_date", System.currentTimeMillis()).apply()
                    auth.currentUser?.uid?.let { uid ->
                        db.collection("users").document(uid).update("terms_agreed", true, "terms_agreed_date", FieldValue.serverTimestamp())
                    }
                }
                Toast.makeText(this@SettingsActivity, "✅ Terms acknowledged", Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun showChangePinDialog() {
        val input = EditText(this).apply {
            hint = "Enter 4-digit PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("🔢 Change SOS PIN")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newPin = input.text.toString().trim()
                if (newPin.length == 4) {
                    sharedPreferences.edit().putString("user_pin", newPin).apply()
                    Toast.makeText(this, "✅ PIN updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Enter 4 digits", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showResetPasswordDialog() {
        val input = EditText(this).apply {
            hint = "Enter your email"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("🔐 Reset Password")
            .setView(input)
            .setPositiveButton("Send") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) {
                    auth.sendPasswordResetEmail(email).addOnSuccessListener {
                        Toast.makeText(this, "📩 Reset email sent", Toast.LENGTH_LONG).show()
                    }
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("⚠ Logout")
            .setMessage("Are you sure?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                sharedPreferences.edit().clear().apply()
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.setNegativeButton("Cancel", null).show()
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }
}