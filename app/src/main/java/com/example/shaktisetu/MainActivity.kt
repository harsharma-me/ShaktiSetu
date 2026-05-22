package com.example.shaktisetu

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private var savedLat = 0.0

    private var savedLng = 0.0

    // Permission Launcher
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

        setContentView(
            R.layout.activity_main
        )

        BottomNavHelper.setup(
            this,
            "home"
        )

        fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        initializeViews()

        requestLocationPermission()
    }

    // Initialize Views
    private fun initializeViews() {

        val btnSOS =
            findViewById<FrameLayout>(
                R.id.btnSOS
            )

        val cardAmbulance =
            findViewById<LinearLayout>(
                R.id.cardAmbulance
            )

        val cardPolice =
            findViewById<LinearLayout>(
                R.id.cardPolice
            )

        val cardWomenSafety =
            findViewById<LinearLayout>(
                R.id.cardWomenSafety
            )

        val cardFakeCall =
            findViewById<LinearLayout>(
                R.id.cardFakeCall
            )

        val locationBar =
            findViewById<LinearLayout>(
                R.id.locationBar
            )

        // SOS
        btnSOS.setOnClickListener {
            openSOSScreen()
        }

        // Emergency Calls
        cardAmbulance.setOnClickListener {
            dialNumber("108")
        }

        cardPolice.setOnClickListener {
            dialNumber("100")
        }

        cardWomenSafety.setOnClickListener {
            dialNumber("1091")
        }

        // Fake Call
        cardFakeCall.setOnClickListener {

            openScreen(
                FakeCallActivity::class.java
            )
        }

        // Location
        locationBar.setOnClickListener {

            Toast.makeText(
                this,
                "📍 Live location sharing coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Permission
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

    // Fetch Location
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

    // Open SOS
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

    // Dial Number
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

    // Open Screen
    private fun openScreen(
        target: Class<*>
    ) {

        val intent = Intent(
            this,
            target
        )

        startAnimatedActivity(intent)
    }

    // Shared Animation
    private fun startAnimatedActivity(
        intent: Intent
    ) {

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