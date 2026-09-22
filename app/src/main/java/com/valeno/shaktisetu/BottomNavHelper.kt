package com.valeno.shaktisetu

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt

object BottomNavHelper {

    // Flag to suppress activity transitions during tab switches
    var isNavigatingTabs = false

    fun setup(
        activity: Activity,
        activeTab: String
    ) {

        val navEvidence =
            activity.findViewById<ImageView>(
                R.id.navEvidence
            ) ?: return

        val navContacts =
            activity.findViewById<ImageView>(
                R.id.navContacts
            )

        val navHomeFab =
            activity.findViewById<LinearLayout>(
                R.id.navHomeFab
            )

        val navCall =
            activity.findViewById<ImageView>(
                R.id.navCall
            )

        val navSettings =
            activity.findViewById<ImageView>(
                R.id.navSettings
            )

        // New Premium Color Palette
        val activeColor =
            "#8B5872".toColorInt()

        val inactiveColor =
            "#AAA4A7".toColorInt()

        // Active States
        navEvidence.setColorFilter(
            if (activeTab == "evidence")
                activeColor
            else
                inactiveColor
        )

        navContacts.setColorFilter(
            if (activeTab == "contacts")
                activeColor
            else
                inactiveColor
        )

        navCall.setColorFilter(
            if (activeTab == "call")
                activeColor
            else
                inactiveColor
        )

        navSettings.setColorFilter(
            if (activeTab == "settings")
                activeColor
            else
                inactiveColor
        )

        // Navigation
        navEvidence.setOnClickListener {

            if (activeTab != "evidence") {

                navigate(
                    activity,
                    EvidenceActivity::class.java
                )
            }
        }

        navContacts.setOnClickListener {

            if (activeTab != "contacts") {

                navigate(
                    activity,
                    ContactsActivity::class.java
                )
            }
        }

        navHomeFab.setOnClickListener {

            if (activeTab != "home") {

                navigate(
                    activity,
                    MainActivity::class.java
                )
            }
        }

        navCall.setOnClickListener {

            if (activeTab != "call") {

                navigate(
                    activity,
                    FakeCallActivity::class.java
                )
            }
        }

        navSettings.setOnClickListener {

            if (activeTab != "settings") {

                navigate(
                    activity,
                    SettingsActivity::class.java
                )
            }
        }
    }

    fun handleTabClick(activity: Activity, tab: String) {
        val targetClass = when (tab) {
            "evidence" -> EvidenceActivity::class.java
            "contacts" -> ContactsActivity::class.java
            "home" -> MainActivity::class.java
            "call" -> FakeCallActivity::class.java
            "settings" -> SettingsActivity::class.java
            else -> null
        }
        targetClass?.let { navigate(activity, it) }
    }

    private fun navigate(
        activity: Activity,
        targetClass: Class<*>
    ) {
        if (activity.javaClass == targetClass) return
        
        isNavigatingTabs = true
        val intent = Intent(activity, targetClass)
        // Keep activities in memory and reorder them for nearly instant switching
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        
        val options = ActivityOptions.makeCustomAnimation(
            activity,
            R.anim.zoom_fade_in,
            R.anim.zoom_fade_out
        )
        
        activity.startActivity(intent, options.toBundle())
        
        // We no longer finish() the activities. This allows them to stay in memory
        // and makes switching between tabs much faster (no recreation).
        activity.window.decorView.postDelayed({
            isNavigatingTabs = false
        }, 300)
    }
}