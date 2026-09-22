package com.valeno.shaktisetu

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.valeno.shaktisetu.database.AppDatabase
import com.valeno.shaktisetu.models.EmergencyContact
import com.valeno.shaktisetu.ui.screens.ContactsScreen
import kotlinx.coroutines.launch

class ContactsActivity : AppCompatActivity() {

    private val contactsList = mutableStateListOf<EmergencyContact>()
    private lateinit var database: AppDatabase

    private val contactLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadContacts()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)
        loadContacts()

        setContent {
            UpdateManager.CheckForUpdates(this)
            ContactsScreen(
                contacts = contactsList,
                onBackClick = { finish() },
                onAddClick = {
                    val intent = Intent(this, AddEditContactActivity::class.java)
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        this, R.anim.zoom_fade_in, R.anim.zoom_fade_out
                    )
                    contactLauncher.launch(intent, options)
                },
                onEditClick = { contact -> showEditDialog(contact) },
                onDeleteClick = { contact -> deleteContact(contact) },
                onTabClick = { tab -> BottomNavHelper.handleTabClick(this, tab) }
            )
        }
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            try {
                val contacts = database.emergencyContactDao().getAllContacts()
                contactsList.clear()
                contactsList.addAll(contacts)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ContactsActivity, "❌ Failed to load contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(contact: EmergencyContact) {
        val intent = Intent(this, AddEditContactActivity::class.java).apply {
            putExtra("is_edit", true)
            putExtra("contact_id", contact.id)
            putExtra("contact_name", contact.contact_name)
            putExtra("contact_phone", contact.contact_phone)
            putExtra("contact_relation", contact.contact_relation)
        }

        val options = ActivityOptionsCompat.makeCustomAnimation(
            this, R.anim.zoom_fade_in, R.anim.zoom_fade_out
        )
        contactLauncher.launch(intent, options)
    }

    private fun deleteContact(contact: EmergencyContact) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Remove ${contact.contact_name}?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.emergencyContactDao().deleteContact(contact)
                        Toast.makeText(this@ContactsActivity, "🗑️ Contact Deleted!", Toast.LENGTH_SHORT).show()
                        loadContacts()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@ContactsActivity, "❌ Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
