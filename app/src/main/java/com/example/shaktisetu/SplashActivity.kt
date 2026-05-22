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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        val mode = sharedPref.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_splash
        )

        val ivLogo =
            findViewById<ImageView>(
                R.id.ivLogo
            )

        val tvAppName =
            findViewById<TextView>(
                R.id.tvAppName
            )

        val tvTagline =
            findViewById<TextView>(
                R.id.tvTagline
            )

        val premiumLoader =
            findViewById<View>(
                R.id.premiumLoader
            )

        startAnimations(
            ivLogo,
            tvAppName,
            tvTagline,
            premiumLoader
        )

        lifecycleScope.launch {

            delay(2200)

            navigateNext()
        }
    }

    // =========================
    // ANIMATIONS
    // =========================

    private fun startAnimations(
        ivLogo: ImageView,
        tvAppName: TextView,
        tvTagline: TextView,
        premiumLoader: View
    ) {

        val logoScaleX =
            ObjectAnimator.ofFloat(
                ivLogo,
                View.SCALE_X,
                0.7f,
                1f
            )

        val logoScaleY =
            ObjectAnimator.ofFloat(
                ivLogo,
                View.SCALE_Y,
                0.7f,
                1f
            )

        val logoAlpha =
            ObjectAnimator.ofFloat(
                ivLogo,
                View.ALPHA,
                0f,
                1f
            )

        val logoSet = AnimatorSet().apply {

            playTogether(
                logoScaleX,
                logoScaleY,
                logoAlpha
            )

            duration = 700

            interpolator =
                OvershootInterpolator(1.3f)
        }

        val nameAlpha =
            ObjectAnimator.ofFloat(
                tvAppName,
                View.ALPHA,
                0f,
                1f
            )

        val nameTranslation =
            ObjectAnimator.ofFloat(
                tvAppName,
                View.TRANSLATION_Y,
                40f,
                0f
            )

        val nameSet = AnimatorSet().apply {

            playTogether(
                nameAlpha,
                nameTranslation
            )

            duration = 500

            startDelay = 300
        }

        val taglineAlpha =
            ObjectAnimator.ofFloat(
                tvTagline,
                View.ALPHA,
                0f,
                1f
            )

        val taglineTranslation =
            ObjectAnimator.ofFloat(
                tvTagline,
                View.TRANSLATION_Y,
                25f,
                0f
            )

        val taglineSet = AnimatorSet().apply {

            playTogether(
                taglineAlpha,
                taglineTranslation
            )

            duration = 450

            startDelay = 550
        }

        val bottomAlpha =
            ObjectAnimator.ofFloat(
                premiumLoader,
                View.ALPHA,
                0f,
                1f
            ).apply {

                duration = 350

                startDelay = 850
            }

        AnimatorSet().apply {

            playTogether(
                logoSet,
                nameSet,
                taglineSet,
                bottomAlpha
            )

            start()
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
