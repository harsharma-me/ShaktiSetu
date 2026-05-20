package com.example.shaktisetu

import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences:
            SharedPreferences

    private lateinit var auth:
            FirebaseAuth

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_settings
        )

        BottomNavHelper.setup(
            this,
            "settings"
        )

        auth = FirebaseAuth.getInstance()

        sharedPreferences =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        val btnChangePassword =
            findViewById<LinearLayout>(
                R.id.btnChangePassword
            )

        val btnInfo =
            findViewById<LinearLayout>(
                R.id.btninfo
            )

        val switchShake =
            findViewById<SwitchCompat>(
                R.id.switchShake
            )

        val switchVoice =
            findViewById<SwitchCompat>(
                R.id.switchVoice
            )

        val tvShakeTest =
            findViewById<TextView>(
                R.id.tvShakeTest
            )

        val tvVoiceTest =
            findViewById<TextView>(
                R.id.tvVoiceTest
            )

        val btnTerms =
            findViewById<LinearLayout>(
                R.id.btnTerms
            )

        val btnLogout =
            findViewById<LinearLayout>(
                R.id.btnDeleteAccount
            )

        val btnChangePin =
            findViewById<LinearLayout>(
                R.id.btnChangePin
            )

        // =========================
        // CHANGE PIN
        // =========================
        btnChangePin.setOnClickListener {
            showChangePinDialog()
        }

        // =========================
        // THEME SELECTION
        // =========================
        val rgTheme = findViewById<RadioGroup>(R.id.rgTheme)
        val currentTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> rgTheme.check(R.id.rbLight)
            AppCompatDelegate.MODE_NIGHT_YES -> rgTheme.check(R.id.rbDark)
            else -> rgTheme.check(R.id.rbSystem)
        }

        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            
            sharedPreferences.edit().putInt("theme_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            
            val themeName = when (mode) {
                AppCompatDelegate.MODE_NIGHT_NO -> "Light"
                AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
                else -> "System"
            }
            Toast.makeText(this, "Theme set to $themeName", Toast.LENGTH_SHORT).show()
        }

        // =========================
        // PROFILE
        // =========================

        btnInfo.setOnClickListener {

            startActivity(

                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )
        }

        // =========================
        // PASSWORD RESET
        // =========================

        btnChangePassword.setOnClickListener {

            showResetPasswordDialog()
        }

        // =========================
        // SHAKE DETECTION
        // =========================

        switchShake.isChecked =
            sharedPreferences.getBoolean(
                "shake_detection",
                true
            )

        switchShake.setOnCheckedChangeListener {
                _,
                isChecked ->

            sharedPreferences.edit()

                .putBoolean(
                    "shake_detection",
                    isChecked
                )

                .apply()

            Toast.makeText(
                this,
                if (isChecked)
                    "📳 Shake Detection ON"
                else
                    "📳 Shake Detection OFF",
                Toast.LENGTH_SHORT
            ).show()
        }

        tvShakeTest.setOnClickListener {

            Toast.makeText(
                this,
                "📳 Shake trigger simulated",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // VOICE DETECTION
        // =========================

        switchVoice.isChecked =
            sharedPreferences.getBoolean(
                "voice_detection",
                false
            )

        switchVoice.setOnCheckedChangeListener {
                _,
                isChecked ->

            sharedPreferences.edit()

                .putBoolean(
                    "voice_detection",
                    isChecked
                )

                .apply()

            Toast.makeText(
                this,
                if (isChecked)
                    "🎙 Voice Detection ON"
                else
                    "🎙 Voice Detection OFF",
                Toast.LENGTH_SHORT
            ).show()
        }

        tvVoiceTest.setOnClickListener {

            Toast.makeText(
                this,
                "🎙 Voice trigger simulated",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // TERMS
        // =========================

        btnTerms.setOnClickListener {

            showTermsDialog()
        }

        // =========================
        // LOGOUT
        // =========================

        btnLogout.setOnClickListener {

            showLogoutDialog()
        }
    }

    // =========================
    // TERMS
    // =========================

    private fun showTermsDialog() {

        val dialog =
            TermsConditionsDialog(this) {

                sharedPreferences.edit()

                    .putBoolean(
                        "terms_agreed",
                        true
                    )

                    .putLong(
                        "terms_agreed_date",
                        System.currentTimeMillis()
                    )

                    .apply()

                Toast.makeText(
                    this,
                    "✅ Terms acknowledged",
                    Toast.LENGTH_SHORT
                ).show()
            }

        dialog.show()
    }

    // =========================
    // RESET PASSWORD
    // =========================

    private fun showChangePinDialog() {
        val input = EditText(this).apply {
            hint = "Enter 4-digit PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            setPadding(32, 24, 32, 24)
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("🔢 Change SOS PIN")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newPin = input.text.toString().trim()
                if (newPin.length == 4) {
                    sharedPreferences.edit().putString("user_pin", newPin).apply()
                    Toast.makeText(this, "✅ PIN updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Please enter a 4-digit PIN", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetPasswordDialog() {

        val input = EditText(this).apply {

            hint = "Enter your email"

            inputType =
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            setPadding(
                32,
                24,
                32,
                24
            )
        }

        AlertDialog.Builder(
            this,
            R.style.DialogTheme
        )

            .setTitle("🔐 Reset Password")

            .setView(input)

            .setPositiveButton("Send") {
                    _,
                    _ ->

                val email =
                    input.text
                        .toString()
                        .trim()

                if (email.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Enter email",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    sendResetEmail(email)
                }
            }

            .setNegativeButton(
                "Cancel",
                null
            )

            .show()
    }

    private fun sendResetEmail(
        email: String
    ) {

        auth.sendPasswordResetEmail(email)

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "📩 Reset email sent",
                    Toast.LENGTH_LONG
                ).show()
            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "❌ Failed to send email",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // =========================
    // LOGOUT
    // =========================

    private fun showLogoutDialog() {

        AlertDialog.Builder(
            this,
            R.style.DialogTheme
        )

            .setTitle("⚠ Logout")

            .setMessage(
                "Are you sure you want to logout?"
            )

            .setPositiveButton("Logout") {
                    _,
                    _ ->

                performLogout()
            }

            .setNegativeButton(
                "Cancel",
                null
            )

            .show()
    }

    private fun performLogout() {

        auth.signOut()

        sharedPreferences.edit()

            .clear()

            .apply()

        Toast.makeText(
            this,
            "✅ Logged out",
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(
            this,
            SignInActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

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

        finish()
    }

    override fun finish() {

        super.finish()

        overridePendingTransition(
            R.anim.zoom_fade_in_back,
            R.anim.zoom_fade_out_back
        )
    }
}