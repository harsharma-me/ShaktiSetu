package com.example.shaktisetu

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.example.shaktisetu.ui.screens.SignUpScreen
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme

class SignUpActivity : AppCompatActivity() {

    private val termsAgreed = mutableStateOf(false)
    private val isLoading = mutableStateOf(false)

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            ShaktiSetuTheme {
                SignUpScreen(
                    onSignUpClick = { name, email, phone, password, confirmPassword ->
                        validateAndSignUp(name, email, phone, password, confirmPassword)
                    },
                    onSignInClick = { finish() },
                    onTermsClick = { showTermsDialog() },
                    onGoogleSignUpClick = {
                        if (!termsAgreed.value) {
                            showToast("Please agree to Terms & Conditions first!")
                        } else {
                            isLoading.value = true
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        }
                    },
                    termsAgreed = termsAgreed.value,
                    isLoading = isLoading.value
                )
            }
        }
    }

    private fun validateAndSignUp(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        when {
            name.isEmpty() -> showToast("Please enter your name!")
            name.length < 3 -> showToast("Name must be at least 3 characters!")
            email.isEmpty() -> showToast("Please enter your email!")
            !isValidEmail(email) -> showToast("Enter valid email!")
            phone.isEmpty() -> showToast("Please enter phone number!")
            !isValidPhone(phone) -> showToast("Enter valid 10-digit number!")
            password.isEmpty() -> showToast("Please enter password!")
            password.length < 6 -> showToast("Password must be at least 6 characters!")
            confirmPassword.isEmpty() -> showToast("Please confirm password!")
            password != confirmPassword -> showToast("Passwords do not match!")
            !termsAgreed.value -> showToast("Please agree to Terms & Conditions")
            else -> {
                isLoading.value = true
                createFirebaseAccount(name, email, phone, password)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val email = user?.email ?: ""
                    val uid = user?.uid ?: ""
                    val name = user?.displayName ?: "User"
                    val phone = user?.phoneNumber ?: ""

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
                    saveUserData(name, email, phone, uid)
                } else {
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

    private fun createFirebaseAccount(
        name: String,
        email: String,
        phone: String,
        password: String
    ) {

        auth.createUserWithEmailAndPassword(
            email,
            password
        )

            .addOnCompleteListener { task ->

                isLoading.value = false

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

    private fun saveUserData(
        name: String,
        email: String,
        phone: String,
        uid: String
    ) {

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
            }
            .addOnFailureListener { e ->
                showToast("Firestore Error: ${e.message}")
            }
    }

    private fun isValidEmail(
        email: String
    ): Boolean {

        return Patterns.EMAIL_ADDRESS
            .matcher(email)
            .matches()
    }

    private fun isValidPhone(
        phone: String
    ): Boolean {

        return phone.length == 10 &&
                phone.all {
                    it.isDigit()
                }
    }

    private fun showTermsDialog() {

        val dialog =
            TermsConditionsDialog(this) {

                termsAgreed.value = true

            }

        dialog.show()
    }

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
