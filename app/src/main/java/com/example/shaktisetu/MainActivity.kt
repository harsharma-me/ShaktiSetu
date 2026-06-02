package com.example.shaktisetu

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import androidx.activity.compose.setContent
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private lateinit var sharedPrefs: SharedPreferences

    private var savedLat = 0.0

    private var savedLng = 0.0

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission denied. You won't receive alerts.", Toast.LENGTH_SHORT).show()
            }
        }

    private val locationPermissionLauncher =

        registerForActivityResult(

            ActivityResultContracts
                .RequestMultiplePermissions()

        ) { permissions ->

            val granted =
                permissions[
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ] == true

            if (granted) {

                fetchLocation()

            } else {

                Toast.makeText(
                    this,
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContent {
            ShaktiSetuTheme {
                MainScreen(
                    onNotificationClick = { openScreen(NotificationActivity::class.java) },
                    onSOSClick = { openSOSScreen() },
                    onAmbulanceClick = { dialNumber("108") },
                    onPoliceClick = { dialNumber("100") },
                    onWomenSafetyClick = { dialNumber("1091") },
                    onFakeCallClick = { openScreen(FakeCallActivity::class.java) },
                    onTabClick = { tab ->
                        when (tab) {
                            "home" -> Unit
                            "evidence" -> openScreen(EvidenceActivity::class.java)
                            "contacts" -> openScreen(ContactsActivity::class.java)
                            "call" -> openScreen(FakeCallActivity::class.java)
                            "settings" -> openScreen(SettingsActivity::class.java)
                        }
                    }
                )
            }
        }

        sharedPrefs = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)

        fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        saveFcmToken()
        listenForBroadcasts()
        requestNotificationPermission()
        requestLocationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun initializeViews() {}
    private fun startPulseAnimation() {}

    private fun saveFcmToken() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf("fcmToken" to token)
                db.collection("users").document(user.uid)
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("FCM", "Token updated: $token")
                    }
            }
        }
    }

    private fun requestLocationPermission() {

        val granted =

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission
                    .ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (granted) {

            fetchLocation()

        } else {

            locationPermissionLauncher.launch(

                arrayOf(
                    Manifest.permission
                        .ACCESS_FINE_LOCATION,

                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocation() {

        try {

            val granted =

                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            if (!granted) return

            fusedLocationClient
                .lastLocation

                .addOnSuccessListener { location ->

                    if (location != null) {

                        savedLat =
                            location.latitude

                        savedLng =
                            location.longitude
                    }
                }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun openSOSScreen() {

        val sharedPref =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        val userEmail =
            sharedPref.getString(
                "user_email",
                ""
            ) ?: ""

        val intent = Intent(
            this,
            SosAlertActivity::class.java
        )

        intent.putExtra(
            "user_email",
            userEmail
        )

        intent.putExtra(
            "latitude",
            savedLat
        )

        intent.putExtra(
            "longitude",
            savedLng
        )

        startAnimatedActivity(intent)
    }

    private fun dialNumber(
        number: String
    ) {

        val intent = Intent(
            Intent.ACTION_DIAL
        )

        intent.data =
            Uri.parse("tel:$number")

        startActivity(intent)
    }

    private fun openScreen(
        target: Class<*>
    ) {

        val intent = Intent(
            this,
            target
        )

        startAnimatedActivity(intent)
    }

    private fun startAnimatedActivity(
        intent: Intent
    ) {
        val options = ActivityOptions.makeSceneTransitionAnimation(this)
        startActivity(intent, options.toBundle())
    }

    private fun listenForBroadcasts() {
        val db = FirebaseFirestore.getInstance()
        val appStartTime = System.currentTimeMillis() / 1000
        Log.d("MainActivity", "Listening for notifications...")
        
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("MainActivity", "Firestore error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val title = doc.getString("title") ?: "Alert"
                    val body = doc.getString("body") ?: ""
                    val timestamp = doc.getTimestamp("timestamp")

                    if (timestamp != null && timestamp.seconds > (appStartTime - 10)) {
                        val target = doc.getString("target") ?: "all"
                        val targetUid = doc.getString("uid")
                        val currentUser = FirebaseAuth.getInstance().currentUser

                        var shouldShow = false
                        if (target == "all") {
                            shouldShow = true
                        } else if (target == "specific" && targetUid != null && currentUser != null) {
                            if (currentUser.uid == targetUid) {
                                shouldShow = true
                            }
                        } else if (target == "emergency") {
                            val isSosActive = sharedPrefs.getBoolean("sos_active", false)
                            if (isSosActive) {
                                shouldShow = true
                            }
                        }
                        
                        if (shouldShow) {
                            Log.d("MainActivity", "New notification received: $title")
                            
                            runOnUiThread {
                                Toast.makeText(this, "📢 $title: $body", Toast.LENGTH_LONG).show()
                            }
                            
                            showLocalNotification(title, body)
                        }
                    }
                }
            }
    }

    private fun showLocalNotification(title: String, body: String) {
        val channelId = "emergency_broadcasts"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId, "Safety Broadcasts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Emergency alerts from admin"
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_sos_shield)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun finish() {
        super.finish()
        if (BottomNavHelper.isNavigatingTabs) return

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