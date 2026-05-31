package com.example.shaktisetu

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.shaktisetu.ui.screens.SplashScreen
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme

class SplashActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        val mode = sharedPref.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)

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
