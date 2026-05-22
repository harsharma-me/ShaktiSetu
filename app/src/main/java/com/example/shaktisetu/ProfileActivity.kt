package com.example.shaktisetu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnBack: ImageButton
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var profileProgressBar: ProgressBar
    private lateinit var tvProgressText: TextView
    private lateinit var layoutNotifications: LinearLayout
    private lateinit var layoutUpdates: LinearLayout
    private lateinit var btnCheckUpdates: Button

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_profile
        )

        BottomNavHelper.setup(
            this,
            "settings"
        )

        sharedPreferences =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        initializeViews()

        loadProfile()

        setupClickListeners()
    }

    // Initialize Views
    private fun initializeViews() {

        tvName =
            findViewById(R.id.tvName)

        tvEmail =
            findViewById(R.id.tvEmail)

        tvPhone =
            findViewById(R.id.tvPhone)

        tvAddress =
            findViewById(R.id.tvAddress)

        btnEditProfile =
            findViewById(
                R.id.btnEditProfile
            )

        btnBack =
            findViewById(R.id.btnBack)

        profileProgressBar = findViewById(R.id.profileProgressBar)
        tvProgressText = findViewById(R.id.tvProgressText)
        layoutNotifications = findViewById(R.id.layoutNotifications)
        layoutUpdates = findViewById(R.id.layoutUpdates)
        btnCheckUpdates = findViewById(R.id.btnCheckUpdates)
    }

    // Click Listeners
    private fun setupClickListeners() {

        btnBack.setOnClickListener {
            finish()
        }

        btnEditProfile.setOnClickListener {

            startActivity(

                Intent(
                    this,
                    EditProfileActivity::class.java
                )
            )
        }

        layoutNotifications.setOnClickListener {
            Toast.makeText(this, "🔔 No new notifications", Toast.LENGTH_SHORT).show()
        }

        btnCheckUpdates.setOnClickListener {
            Toast.makeText(this, "✨ You are on the latest version!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {

        super.onResume()

        loadProfile()
    }

    // Load Profile
    private fun loadProfile() {

        val name =
            sharedPreferences.getString(
                "user_name",
                ""
            ) ?: ""

        val email =
            sharedPreferences.getString(
                "user_email",
                ""
            ) ?: ""

        val phone =
            sharedPreferences.getString(
                "user_phone",
                ""
            ) ?: ""

        val address =
            sharedPreferences.getString(
                "user_address",
                ""
            ) ?: ""

        tvName.text = if (name.isNotBlank()) name else "User"

        tvEmail.text = if (email.isNotBlank()) email else "user@example.com"

        tvPhone.text = if (phone.isNotBlank()) phone else "Not provided"

        tvAddress.text = if (address.isNotBlank()) address else "Not provided"

        updateProfileProgress(name, email, phone, address)
    }

    private fun updateProfileProgress(name: String, email: String, phone: String, address: String) {
        var completedFields = 0
        val totalFields = 4

        if (name.isNotBlank()) completedFields++
        if (email.isNotBlank()) completedFields++
        if (phone.isNotBlank()) completedFields++
        if (address.isNotBlank()) completedFields++

        val progress = (completedFields * 100) / totalFields
        profileProgressBar.progress = progress
        tvProgressText.text = getString(R.string.progress_text_format, progress)
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