package com.example.shaktisetu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LandingActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_landing
        )

        lifecycleScope.launch {

            // Splash Delay
            delay(2500)

            navigateNext()
        }
    }

    // Navigate
    private fun navigateNext() {

        val sharedPref =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        val savedEmail =
            sharedPref.getString(
                "user_email",
                ""
            ) ?: ""

        val nextScreen =
            if (savedEmail.isNotEmpty()) {

                MainActivity::class.java

            } else {

                SignInActivity::class.java
            }

        startActivity(
            Intent(
                this,
                nextScreen
            )
        )

        overridePendingTransition(
            R.anim.zoom_fade_in,
            R.anim.zoom_fade_out
        )

        finish()
    }
}