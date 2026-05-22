package com.example.shaktisetu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var loadingOverlay: FrameLayout

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    toggleLoading(false)
                    Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                toggleLoading(false)
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_sign_in
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        loadingOverlay = findViewById(R.id.loadingOverlay)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

        val btnGoogle =
            findViewById<Button>(
                R.id.btnGoogle
            )

        // Google Login
        btnGoogle.setOnClickListener {
            toggleLoading(true)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

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

            toggleLoading(true)

            signInUser(
                email,
                password
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
        password: String
    ) {

        auth.signInWithEmailAndPassword(
            email,
            password
        )

            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    fetchAndSaveUserData(email)

                } else {
                    toggleLoading(false)
                    Toast.makeText(
                        this,
                        "❌ Invalid Email or Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Google Auth
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val email = user?.email ?: ""
                    checkIfUserExistsInFirestore(email)
                } else {
                    toggleLoading(false)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun toggleLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun checkIfUserExistsInFirestore(email: String) {
        val uid = auth.currentUser?.uid ?: run {
            toggleLoading(false)
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User exists, just fetch data
                    fetchAndSaveUserData(email)
                } else {
                    // New user from Google, create profile
                    val user = auth.currentUser
                    val name = user?.displayName ?: "User"
                    val phone = user?.phoneNumber ?: ""

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
                            // After creating profile, go to PIN creation
                            saveLocalDataAndNavigate(email, uid, name, phone, "")
                        }
                        .addOnFailureListener {
                            toggleLoading(false)
                            Toast.makeText(this, "Failed to create user profile", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                toggleLoading(false)
                Toast.makeText(this, "Error checking user existence", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch User Data from Firestore
    private fun fetchAndSaveUserData(email: String) {
        val uid = auth.currentUser?.uid ?: run {
            toggleLoading(false)
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val address = document.getString("address") ?: ""
                    saveLocalDataAndNavigate(email, uid, name, phone, address)
                } else {
                    saveLocalDataAndNavigate(email, uid, "", "", "")
                }
            }
            .addOnFailureListener {
                toggleLoading(false)
                Toast.makeText(this, "❌ Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveLocalDataAndNavigate(email: String, uid: String, name: String, phone: String, address: String) {
        val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val termsAgreed = document.getBoolean("terms_agreed") ?: false
                
                sharedPref.edit {
                    putString("user_name", name)
                    putString("user_phone", phone)
                    putString("user_address", address)
                    putString("user_email", email)
                    putString("user_uid", uid)
                    putBoolean("terms_agreed", termsAgreed)
                    putBoolean("is_logged_in", true)
                }

                val savedPin = sharedPref.getString("user_pin", "")
                val nextActivity = if (savedPin.isNullOrEmpty()) {
                    CreatePinActivity::class.java
                } else {
                    MainActivity::class.java
                }

                Toast.makeText(this, "✅ Login Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, nextActivity).apply {
                    putExtra("user_email", email)
                })
                finish()
            }
            .addOnFailureListener {
                // Fallback to local if fetch fails
                sharedPref.edit {
                    putString("user_name", name)
                    putString("user_phone", phone)
                    putString("user_address", address)
                    putString("user_email", email)
                    putString("user_uid", uid)
                    putBoolean("is_logged_in", true)
                }
                
                val savedPin = sharedPref.getString("user_pin", "")
                val nextActivity = if (savedPin.isNullOrEmpty()) {
                    CreatePinActivity::class.java
                } else {
                    MainActivity::class.java
                }

                Toast.makeText(this, "✅ Login Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, nextActivity).apply {
                    putExtra("user_email", email)
                })
                finish()
            }
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