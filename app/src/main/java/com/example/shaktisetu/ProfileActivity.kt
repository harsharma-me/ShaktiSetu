package com.example.shaktisetu

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.shaktisetu.ui.screens.ProfileScreen
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BottomNavHelper.setup(this, "settings")

        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        val sharedPreferences = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        val name = sharedPreferences.getString("user_name", "") ?: ""
        val email = sharedPreferences.getString("user_email", "") ?: ""
        val phone = sharedPreferences.getString("user_phone", "") ?: ""
        val address = sharedPreferences.getString("user_address", "") ?: ""

        setContent {
            ShaktiSetuTheme {
                ProfileScreen(
                    name = name,
                    email = email,
                    phone = phone,
                    address = address,
                    onBackClick = { finish() },
                    onEditClick = {
                        startActivity(Intent(this, EditProfileActivity::class.java))
                    }
                )
            }
        }
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
