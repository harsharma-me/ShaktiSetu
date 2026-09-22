package com.valeno.shaktisetu

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.valeno.shaktisetu.ui.screens.WebScreen
import com.valeno.shaktisetu.ui.theme.ShaktiSetuTheme

class WebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title") ?: "Information"
        val url = intent.getStringExtra("url") ?: "https://valeno-legal.vercel.app/"

        setContent {
            ShaktiSetuTheme {
                UpdateManager.CheckForUpdates(this)
                WebScreen(
                    title = title,
                    url = url,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
