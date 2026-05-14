package com.example.shaktisetu

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName:
            EditText

    private lateinit var etEmail:
            EditText

    private lateinit var etPhone:
            EditText

    private lateinit var etAddress:
            EditText

    private lateinit var btnUpdate:
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
            R.layout.activity_edit_profile
        )

        sharedPreferences =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        etName =
            findViewById(R.id.etName)

        etEmail =
            findViewById(R.id.etEmail)

        etPhone =
            findViewById(R.id.etPhone)

        etAddress =
            findViewById(R.id.etAddress)

        btnUpdate =
            findViewById(R.id.btnUpdate)

        btnBack =
            findViewById(R.id.btnBack)

        loadProfileData()

        // Back
        btnBack.setOnClickListener {
            finish()
        }

        // Update
        btnUpdate.setOnClickListener {

            val name =
                etName.text
                    .toString()
                    .trim()

            val phone =
                etPhone.text
                    .toString()
                    .trim()

            val address =
                etAddress.text
                    .toString()
                    .trim()

            if (
                name.isEmpty() ||
                phone.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Please fill required fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            updateProfile(
                name,
                phone,
                address
            )
        }
    }

    // Load Profile
    private fun loadProfileData() {

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
                ""
            ) ?: ""

        val address =
            sharedPreferences.getString(
                "user_address",
                ""
            ) ?: ""

        etName.setText(name)

        etEmail.setText(email)

        etPhone.setText(phone)

        etAddress.setText(address)
    }

    // Update Profile
    private fun updateProfile(
        name: String,
        phone: String,
        address: String
    ) {

        try {

            btnUpdate.isEnabled = false

            btnUpdate.text =
                "Updating..."

            // Save Locally
            sharedPreferences.edit()

                .putString(
                    "user_name",
                    name
                )

                .putString(
                    "user_phone",
                    phone
                )

                .putString(
                    "user_address",
                    address
                )

                .apply()

            Toast.makeText(
                this,
                "✅ Profile Updated!",
                Toast.LENGTH_SHORT
            ).show()

            finish()

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                this,
                "❌ Failed to update profile",
                Toast.LENGTH_SHORT
            ).show()

        } finally {

            btnUpdate.isEnabled = true

            btnUpdate.text =
                "Update Profile"
        }
    }

    override fun finish() {

        super.finish()

        overridePendingTransition(
            R.anim.zoom_fade_in_back,
            R.anim.zoom_fade_out_back
        )
    }
}