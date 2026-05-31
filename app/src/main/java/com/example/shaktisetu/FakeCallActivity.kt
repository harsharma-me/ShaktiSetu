package com.example.shaktisetu

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.shaktisetu.ui.screens.FakeCallScreen

class FakeCallActivity : AppCompatActivity() {

    private var selectedDelay by mutableStateOf(5000L)
    private var callerName by mutableStateOf("Harsh")
    private var phoneNumber by mutableStateOf("+91 82950 00000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FakeCallScreen(
                callerName = callerName,
                phoneNumber = phoneNumber,
                selectedDelay = selectedDelay,
                onCallerNameClick = { showCallerEditDialog() },
                onDelaySelect = { selectedDelay = it },
                onScheduleClick = { scheduleFakeCall() },
                onTabClick = { tab -> BottomNavHelper.handleTabClick(this, tab) }
            )
        }
    }

    private fun showCallerEditDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 0)
        }

        val etName = EditText(this).apply {
            setText(callerName)
            hint = "Caller Name"
        }
        val etPhone = EditText(this).apply {
            setText(phoneNumber)
            hint = "Phone Number"
        }

        layout.addView(etName)
        layout.addView(etPhone)

        AlertDialog.Builder(this)
            .setTitle("Edit Caller Identity")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newPhone = etPhone.text.toString().trim()
                if (newName.isNotEmpty()) callerName = newName
                if (newPhone.isNotEmpty()) phoneNumber = newPhone
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleFakeCall() {
        Toast.makeText(this, "📞 Fake call scheduled in ${selectedDelay/1000}s", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            openIncomingCall()
        }, selectedDelay)
    }

    private fun openIncomingCall() {
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("caller_name", callerName)
            putExtra("phone_number", phoneNumber)
        }

        val options = ActivityOptions.makeCustomAnimation(
            this, R.anim.zoom_fade_in, R.anim.zoom_fade_out
        )
        startActivity(intent, options.toBundle())
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
