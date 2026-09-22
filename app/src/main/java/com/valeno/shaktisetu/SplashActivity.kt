package com.valeno.shaktisetu

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.valeno.shaktisetu.ui.screens.SplashScreen
import com.valeno.shaktisetu.ui.theme.ShaktiSetuTheme

class SplashActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        // Force Light Mode across the app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        setContent {
            ShaktiSetuTheme {
                SplashScreen(onAnimationFinished = {
                    navigateNext()
                })
            }
        }
    }

    // =========================
    // NAVIGATION
    // =========================

    private fun navigateNext() {

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

        val userPin =
            sharedPref.getString(
                "user_pin",
                ""
            ) ?: ""

        val targetActivity =

            if (userEmail.isNotEmpty()) {
                if (userPin.isEmpty()) {
                    CreatePinActivity::class.java
                } else {
                    MainActivity::class.java
                }
            } else {
                SignInActivity::class.java
            }

        val intent = Intent(
            this,
            targetActivity
        )

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

        finish()
    }
}
