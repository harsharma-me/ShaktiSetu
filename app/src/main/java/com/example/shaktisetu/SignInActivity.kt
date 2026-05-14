package com.example.shaktisetu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth:
            FirebaseAuth

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_sign_in
        )

        auth = FirebaseAuth.getInstance()

        val etEmail =
            findViewById<EditText>(
                R.id.etEmail
            )

        val etPassword =
            findViewById<EditText>(
                R.id.etPassword
            )

        val btnContinue =
            findViewById<Button>(
                R.id.btnContinue
            )

        val tvForgot =
            findViewById<TextView>(
                R.id.tvForgot
            )

        val tvSignup =
            findViewById<TextView>(
                R.id.tvSignup
            )

        // Login
        btnContinue.setOnClickListener {

            val email =
                etEmail.text
                    .toString()
                    .trim()
                    .lowercase()

            val password =
                etPassword.text
                    .toString()
                    .trim()

            if (
                email.isEmpty() ||
                password.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Please fill all fields!",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            btnContinue.isEnabled = false

            btnContinue.text =
                "Logging in..."

            signInUser(
                email,
                password,
                btnContinue
            )
        }

        // Signup
        tvSignup.setOnClickListener {

            startActivity(

                Intent(
                    this,
                    SignUpActivity::class.java
                )
            )
        }

        // Forgot Password
        tvForgot.setOnClickListener {

            showForgotPassword(
                etEmail.text.toString().trim()
            )
        }
    }

    // Sign In
    private fun signInUser(
        email: String,
        password: String,
        button: Button
    ) {

        auth.signInWithEmailAndPassword(
            email,
            password
        )

            .addOnCompleteListener { task ->

                button.isEnabled = true

                button.text = "CONTINUE"

                if (task.isSuccessful) {

                    saveUserSession(email)

                    Toast.makeText(
                        this,
                        "✅ Login Successful!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(

                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "❌ Invalid Email or Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Save Session
    private fun saveUserSession(
        email: String
    ) {

        val sharedPref =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        sharedPref.edit()

            .putString(
                "user_email",
                email
            )

            .putBoolean(
                "is_logged_in",
                true
            )

            .apply()
    }

    // Forgot Password
    private fun showForgotPassword(
        email: String
    ) {

        if (email.isEmpty()) {

            Toast.makeText(
                this,
                "Enter email first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

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
                    "❌ Failed to send reset email",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}