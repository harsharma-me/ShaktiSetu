package com.example.shaktisetu

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shaktisetu.database.AppDatabase
import com.example.shaktisetu.models.EmergencyContact
import kotlinx.coroutines.launch

class AddEditContactActivity : AppCompatActivity() {

    private var isEditMode = false
    private var contactId: Int = 0

    private lateinit var database: AppDatabase

    private val relationOptions = listOf(
        "FAMILY",
        "FRIEND",
        "SPOUSE",
        "COLLEAGUE",
        "NEIGHBOR",
        "OTHER"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_edit_contact)

        database = AppDatabase.getDatabase(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
        val spinnerRelation = findViewById<Spinner>(R.id.spinnerRelation)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        // Spinner Setup
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            relationOptions
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerRelation.adapter = adapter

        // Edit Mode Check
        isEditMode = intent.getBooleanExtra(
            "is_edit",
            false
        )

        if (isEditMode) {

            tvTitle.text = "Edit Details"

            contactId = intent.getIntExtra(
                "contact_id",
                0
            )

            etFullName.setText(
                intent.getStringExtra(
                    "contact_name"
                ) ?: ""
            )

            etMobileNumber.setText(
                intent.getStringExtra(
                    "contact_phone"
                ) ?: ""
            )

            val relation = intent.getStringExtra(
                "contact_relation"
            ) ?: ""

            val index = relationOptions.indexOfFirst {
                it.equals(
                    relation,
                    ignoreCase = true
                )
            }

            if (index >= 0) {
                spinnerRelation.setSelection(index)
            }
        }

        // Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // Confirm Button
        btnConfirm.setOnClickListener {

            val name = etFullName.text
                .toString()
                .trim()

            val phone = etMobileNumber.text
                .toString()
                .trim()

            val relation = spinnerRelation
                .selectedItem
                .toString()

            if (name.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter full name!",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (phone.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter mobile number!",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (isEditMode) {

                updateContact(
                    name,
                    phone,
                    relation
                )

            } else {

                addContact(
                    name,
                    phone,
                    relation
                )
            }
        }
    }

    // Add Contact
    private fun addContact(
        name: String,
        phone: String,
        relation: String
    ) {

        lifecycleScope.launch {

            val contact = EmergencyContact(
                contact_name = name,
                contact_phone = phone,
                contact_relation = relation
            )

            database
                .emergencyContactDao()
                .insertContact(contact)

            Toast.makeText(
                this@AddEditContactActivity,
                "✅ Contact Added!",
                Toast.LENGTH_SHORT
            ).show()

            setResult(RESULT_OK)

            finish()
        }
    }

    // Update Contact
    private fun updateContact(
        name: String,
        phone: String,
        relation: String
    ) {

        lifecycleScope.launch {

            val updatedContact = EmergencyContact(
                id = contactId,
                contact_name = name,
                contact_phone = phone,
                contact_relation = relation
            )

            database
                .emergencyContactDao()
                .updateContact(updatedContact)

            Toast.makeText(
                this@AddEditContactActivity,
                "✅ Contact Updated!",
                Toast.LENGTH_SHORT
            ).show()

            setResult(RESULT_OK)

            finish()
        }
    }
}