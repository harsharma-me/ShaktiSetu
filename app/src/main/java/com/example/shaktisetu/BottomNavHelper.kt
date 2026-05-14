package com.example.shaktisetu

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt

object BottomNavHelper {

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

    private fun navigate(
        activity: Activity,
        targetClass: Class<*>
    ) {

        val intent = Intent(
            activity,
            targetClass
        )

        val options =
            ActivityOptions.makeCustomAnimation(
                activity,
                R.anim.zoom_fade_in,
                R.anim.zoom_fade_out
            )

        activity.startActivity(
            intent,
            options.toBundle()
        )

        activity.finish()
    }
}