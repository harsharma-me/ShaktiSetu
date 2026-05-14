package com.example.shaktisetu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName:
            TextView

    private lateinit var tvEmail:
            TextView

    private lateinit var tvPhone:
            TextView

    private lateinit var tvAddress:
            TextView

    private lateinit var btnEditProfile:
            Button

    private lateinit var btnLogout:
            Button

    private lateinit var btnBack:
            ImageButton

    private lateinit var sharedPreferences:
            SharedPreferences

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

        // Optional
//        btnLogout =
//            findViewById(R.id.btnLogout)
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

        btnLogout.setOnClickListener {
            showLogoutDialog()
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
                "User"
            ) ?: "User"

        val email =
            sharedPreferences.getString(
                "user_email",
                "user@example.com"
            ) ?: "user@example.com"

        val phone =
            sharedPreferences.getString(
                "user_phone",
                "Not provided"
            ) ?: "Not provided"

        val address =
            sharedPreferences.getString(
                "user_address",
                "Not provided"
            ) ?: "Not provided"

        tvName.text = name

        tvEmail.text = email

        tvPhone.text = phone

        tvAddress.text = address
    }

    // Logout Dialog
    private fun showLogoutDialog() {

        AlertDialog.Builder(this)

            .setTitle("Logout")

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

    // Logout
    private fun performLogout() {

        sharedPreferences.edit()

            .clear()

            .apply()

        Toast.makeText(
            this,
            "✅ Logged out",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(

            Intent(
                this,
                SignInActivity::class.java
            )
        )

        finishAffinity()
    }

    override fun finish() {

        super.finish()

        overridePendingTransition(
            R.anim.zoom_fade_in_back,
            R.anim.zoom_fade_out_back
        )
    }
}