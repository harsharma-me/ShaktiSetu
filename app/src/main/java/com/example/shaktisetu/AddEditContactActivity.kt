package com.example.shaktisetu

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shaktisetu.database.AppDatabase
import com.example.shaktisetu.models.EmergencyContact
import com.example.shaktisetu.ui.screens.AddEditContactScreen
import kotlinx.coroutines.launch

class AddEditContactActivity : AppCompatActivity() {

    private var isEditMode = false
    private var contactId = 0
    private lateinit var database: AppDatabase
    private val relationOptions = listOf(
        "Father", "Mother", "Brother", "Sister",
        "Husband", "Wife", "Friend", "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)

        isEditMode = intent.getBooleanExtra("is_edit", false)
        val initialName = intent.getStringExtra("contact_name") ?: ""
        val initialPhone = intent.getStringExtra("contact_phone") ?: ""
        val initialRelation = intent.getStringExtra("contact_relation") ?: ""
        contactId = intent.getIntExtra("contact_id", 0)

        setContent {
            AddEditContactScreen(
                isEditMode = isEditMode,
                initialName = initialName,
                initialPhone = initialPhone,
                initialRelation = initialRelation,
                relationOptions = relationOptions,
                onBackClick = { finish() },
                onSaveClick = { name, phone, relation ->
                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (isEditMode) {
                            updateContact(name, phone, relation)
                        } else {
                            addContact(name, phone, relation)
                        }
                    }
                }
            )
        }
    }

    private fun addContact(name: String, phone: String, relation: String) {
        lifecycleScope.launch {
            try {
                database.emergencyContactDao().insertContact(
                    EmergencyContact(
                        contact_name = name,
                        contact_phone = phone,
                        contact_relation = relation
                    )
                )
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@AddEditContactActivity, "❌ Error saving contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateContact(name: String, phone: String, relation: String) {
        lifecycleScope.launch {
            try {
                database.emergencyContactDao().updateContact(
                    EmergencyContact(
                        id = contactId,
                        contact_name = name,
                        contact_phone = phone,
                        contact_relation = relation
                    )
                )
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@AddEditContactActivity, "❌ Error updating contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun finish() {
        super.finish()
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
