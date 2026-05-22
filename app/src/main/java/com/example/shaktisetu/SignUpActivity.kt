package com.example.shaktisetu

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private var termsAgreed = false

    private lateinit var tvTermsLink: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    showToast("Google sign in failed: ${e.message}")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

        val btnGoogle =
            findViewById<Button>(R.id.btnGoogle)

        tvTermsLink =
            findViewById(R.id.tvTermsLink)

        // Google Login
        btnGoogle.setOnClickListener {
            if (!termsAgreed) {
                showToast("Please agree to Terms & Conditions first!")
                tvTermsLink.setTextColor(Color.RED)
                return@setOnClickListener
            }
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

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
                        "Terms & Conditions"
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

    // Google Auth
    private fun firebaseAuthWithGoogle(idToken: String) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val email = user?.email ?: ""
                    val uid = user?.uid ?: ""
                    val name = user?.displayName ?: "User"
                    val phone = user?.phoneNumber ?: ""

                    // For Google Sign-up, we immediately check/save user data
                    checkAndSaveGoogleUser(name, email, phone, uid)
                } else {
                    showToast("❌ Google Authentication Failed.")
                }
            }
    }

    private fun checkAndSaveGoogleUser(name: String, email: String, phone: String, uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // New user, save details
                    saveUserData(name, email, phone, uid)
                } else {
                    // Already exists, just sync local prefs
                    val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
                    sharedPref.edit {
                        putString("user_name", document.getString("name") ?: name)
                        putString("user_email", email)
                        putString("user_phone", document.getString("phone") ?: phone)
                        putString("user_uid", uid)
                        putBoolean("terms_agreed", true)
                        putBoolean("is_logged_in", true)
                    }
                }

                val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
                val savedPin = sharedPref.getString("user_pin", "")
                
                showToast("✅ Login Successful!")
                
                val nextActivity = if (savedPin.isNullOrEmpty()) {
                    CreatePinActivity::class.java
                } else {
                    MainActivity::class.java
                }

                startActivity(Intent(this, nextActivity).apply {
                    putExtra("user_email", email)
                })
                finish()
            }
            .addOnFailureListener { e ->
                showToast("Error checking user: ${e.message}")
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

                    val userId = auth.currentUser?.uid ?: ""

                    saveUserData(
                        name,
                        email,
                        phone,
                        userId
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

    // Save User Data Locally & Firestore
    private fun saveUserData(
        name: String,
        email: String,
        phone: String,
        uid: String
    ) {

        // 1. Save to SharedPreferences (Local)
        val sharedPref =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        sharedPref.edit {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_phone", phone)
            putString("user_uid", uid)
            putBoolean("terms_agreed", true)
            putBoolean("is_logged_in", true)
        }

        // 2. Save to Firestore (Cloud)
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "uid" to uid,
            "address" to "",
            "terms_agreed" to true,
            "terms_agreed_date" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                // Success
            }
            .addOnFailureListener { e ->
                showToast("Firestore Error: ${e.message}")
            }
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
                    "Terms & Conditions"

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