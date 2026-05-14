package com.example.shaktisetu

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private var termsAgreed = false

    private lateinit var tvTermsLink: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val etName =
            findViewById<EditText>(R.id.etName)

        val etEmail =
            findViewById<EditText>(R.id.etEmail)

        val etPhone =
            findViewById<EditText>(R.id.etPhone)

        val etPassword =
            findViewById<EditText>(R.id.etPassword)

        val etConfirmPassword =
            findViewById<EditText>(R.id.etConfirmPassword)

        val btnSignUp =
            findViewById<Button>(R.id.btnSignUp)

        val tvSignIn =
            findViewById<TextView>(R.id.tvSignIn)

        tvTermsLink =
            findViewById(R.id.tvTermsLink)

        // Terms Dialog
        tvTermsLink.setOnClickListener {
            showTermsDialog()
        }

        // Sign Up
        btnSignUp.setOnClickListener {

            val name =
                etName.text
                    .toString()
                    .trim()

            val email =
                etEmail.text
                    .toString()
                    .trim()
                    .lowercase()

            val phone =
                etPhone.text
                    .toString()
                    .trim()

            val password =
                etPassword.text
                    .toString()
                    .trim()

            val confirmPassword =
                etConfirmPassword.text
                    .toString()
                    .trim()

            when {

                name.isEmpty() -> {

                    showToast(
                        "Please enter your name!"
                    )

                    etName.requestFocus()
                }

                name.length < 3 -> {

                    showToast(
                        "Name must be at least 3 characters!"
                    )

                    etName.requestFocus()
                }

                email.isEmpty() -> {

                    showToast(
                        "Please enter your email!"
                    )

                    etEmail.requestFocus()
                }

                !isValidEmail(email) -> {

                    showToast(
                        "Enter valid email!"
                    )

                    etEmail.requestFocus()
                }

                phone.isEmpty() -> {

                    showToast(
                        "Please enter phone number!"
                    )

                    etPhone.requestFocus()
                }

                !isValidPhone(phone) -> {

                    showToast(
                        "Enter valid 10-digit number!"
                    )

                    etPhone.requestFocus()
                }

                password.isEmpty() -> {

                    showToast(
                        "Please enter password!"
                    )

                    etPassword.requestFocus()
                }

                password.length < 6 -> {

                    showToast(
                        "Password must be at least 6 characters!"
                    )

                    etPassword.requestFocus()
                }

                confirmPassword.isEmpty() -> {

                    showToast(
                        "Please confirm password!"
                    )

                    etConfirmPassword.requestFocus()
                }

                password != confirmPassword -> {

                    showToast(
                        "Passwords do not match!"
                    )

                    etConfirmPassword.requestFocus()
                }

                !termsAgreed -> {

                    showToast(
                        "Agree to Terms & Conditions"
                    )

                    tvTermsLink.setTextColor(
                        Color.RED
                    )
                }

                else -> {

                    createFirebaseAccount(
                        name,
                        email,
                        phone,
                        password,
                        btnSignUp
                    )
                }
            }
        }

        // Already Have Account
        tvSignIn.setOnClickListener {
            finish()
        }
    }

    // Firebase Signup
    private fun createFirebaseAccount(
        name: String,
        email: String,
        phone: String,
        password: String,
        button: Button
    ) {

        button.isEnabled = false

        button.text =
            "Creating Account..."

        auth.createUserWithEmailAndPassword(
            email,
            password
        )

            .addOnCompleteListener { task ->

                button.isEnabled = true

                button.text =
                    "CREATE ACCOUNT"

                if (task.isSuccessful) {

                    saveUserData(
                        name,
                        email,
                        phone
                    )

                    showToast(
                        "✅ Account Created!"
                    )

                    val intent = Intent(
                        this,
                        CreatePinActivity::class.java
                    )

                    intent.putExtra(
                        "user_email",
                        email
                    )

                    startActivity(intent)

                    finish()

                } else {

                    val error =
                        task.exception?.message
                            ?: "Signup failed"

                    showToast(
                        "❌ $error"
                    )
                }
            }
    }

    // Save User Data Locally
    private fun saveUserData(
        name: String,
        email: String,
        phone: String
    ) {

        val sharedPref =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        sharedPref.edit()

            .putString(
                "user_name",
                name
            )

            .putString(
                "user_email",
                email
            )

            .putString(
                "user_phone",
                phone
            )

            .putBoolean(
                "terms_agreed",
                true
            )

            .putBoolean(
                "is_logged_in",
                true
            )

            .apply()
    }

    // Email Validation
    private fun isValidEmail(
        email: String
    ): Boolean {

        return Patterns.EMAIL_ADDRESS
            .matcher(email)
            .matches()
    }

    // Phone Validation
    private fun isValidPhone(
        phone: String
    ): Boolean {

        return phone.length == 10 &&
                phone.all {
                    it.isDigit()
                }
    }

    // Terms Dialog
    private fun showTermsDialog() {

        val dialog =
            TermsConditionsDialog(this) {

                termsAgreed = true

                tvTermsLink.setTextColor(
                    Color.parseColor("#4CAF50")
                )

                tvTermsLink.text =
                    "✅ I agree to Terms & Conditions"

                showToast(
                    "✅ Terms accepted!"
                )
            }

        dialog.show()
    }

    // Toast Helper
    private fun showToast(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}