package com.example.shaktisetu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shaktisetu.database.AppDatabase
import com.example.shaktisetu.models.EmergencyContact
import kotlinx.coroutines.launch

class ContactsActivity : AppCompatActivity() {

    private var contactsList =
        mutableListOf<EmergencyContact>()

    private lateinit var adapter:
            ContactsAdapter

    private lateinit var rvContacts:
            RecyclerView

    private lateinit var tvNoContacts:
            TextView

    private lateinit var database:
            AppDatabase

    private val contactLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {
                loadContacts()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        // Set status and navigation bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)

        // Ensure icons are visible based on theme
        val isDarkTheme = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val decorView = window.decorView
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val controller = decorView.windowInsetsController
            if (controller != null) {
                val appearance = if (isDarkTheme) 0 else (android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
                controller.setSystemBarsAppearance(appearance, android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
            }
        } else {
            @Suppress("DEPRECATION")
            var flags = decorView.systemUiVisibility
            if (!isDarkTheme) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }

        setContentView(
            R.layout.activity_contacts
        )

        BottomNavHelper.setup(
            this,
            "contacts"
        )

        database =
            AppDatabase.getDatabase(this)

        val btnBack =
            findViewById<ImageView>(
                R.id.btnBack
            )

        val btnAddMember =
            findViewById<Button>(
                R.id.btnAddMember
            )

        rvContacts =
            findViewById(R.id.rvContacts)

        tvNoContacts =
            findViewById(R.id.tvNoContacts)

        adapter = ContactsAdapter(
            contacts = contactsList,

            onEdit = { contact ->
                showEditDialog(contact)
            },

            onDelete = { contact ->
                deleteContact(contact)
            }
        )

        rvContacts.layoutManager =
            LinearLayoutManager(this)

        rvContacts.adapter = adapter

        loadContacts()

        btnBack.setOnClickListener {
            finish()
        }

        btnAddMember.setOnClickListener {

            val intent = Intent(
                this,
                AddEditContactActivity::class.java
            )

            val options =
                ActivityOptionsCompat
                    .makeCustomAnimation(
                        this,
                        R.anim.zoom_fade_in,
                        R.anim.zoom_fade_out
                    )

            contactLauncher.launch(
                intent,
                options
            )
        }
    }

    // Load Contacts
    private fun loadContacts() {

        lifecycleScope.launch {

            try {

                val contacts =
                    database
                        .emergencyContactDao()
                        .getAllContacts()

                contactsList.clear()

                contactsList.addAll(contacts)

                adapter.notifyDataSetChanged()

                if (contactsList.isEmpty()) {

                    tvNoContacts.visibility =
                        View.VISIBLE

                    rvContacts.visibility =
                        View.GONE

                } else {

                    tvNoContacts.visibility =
                        View.GONE

                    rvContacts.visibility =
                        View.VISIBLE
                }

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    this@ContactsActivity,
                    "❌ Failed to load contacts",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Edit Contact
    private fun showEditDialog(
        contact: EmergencyContact
    ) {

        val intent = Intent(
            this,
            AddEditContactActivity::class.java
        ).apply {

            putExtra("is_edit", true)

            putExtra(
                "contact_id",
                contact.id
            )

            putExtra(
                "contact_name",
                contact.contact_name
            )

            putExtra(
                "contact_phone",
                contact.contact_phone
            )

            putExtra(
                "contact_relation",
                contact.contact_relation
            )
        }

        val options =
            ActivityOptionsCompat
                .makeCustomAnimation(
                    this,
                    R.anim.zoom_fade_in,
                    R.anim.zoom_fade_out
                )

        contactLauncher.launch(
            intent,
            options
        )
    }

    // Delete Contact
    private fun deleteContact(
        contact: EmergencyContact
    ) {

        AlertDialog.Builder(this)

            .setTitle("Delete Contact")

            .setMessage(
                "Remove ${contact.contact_name}?"
            )

            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                lifecycleScope.launch {

                    try {

                        database
                            .emergencyContactDao()
                            .deleteContact(contact)

                        Toast.makeText(
                            this@ContactsActivity,
                            "🗑️ Contact Deleted!",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadContacts()

                    } catch (e: Exception) {

                        e.printStackTrace()

                        Toast.makeText(
                            this@ContactsActivity,
                            "❌ Failed to delete",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            .setNegativeButton(
                "Cancel",
                null
            )

            .show()
    }

    override fun finish() {

        super.finish()

        overridePendingTransition(
            R.anim.zoom_fade_in_back,
            R.anim.zoom_fade_out_back
        )
    }

    // Adapter
    inner class ContactsAdapter(

        private val contacts:
        List<EmergencyContact>,

        private val onEdit:
            (EmergencyContact) -> Unit,

        private val onDelete:
            (EmergencyContact) -> Unit

    ) : RecyclerView.Adapter<
            ContactsAdapter.ContactViewHolder>() {

        inner class ContactViewHolder(
            view: View
        ) : RecyclerView.ViewHolder(view) {

            val tvAvatar:
                    TextView =
                view.findViewById(R.id.tvAvatar)

            val tvContactName:
                    TextView =
                view.findViewById(R.id.tvContactName)

            val tvContactDetail:
                    TextView =
                view.findViewById(R.id.tvContactDetail)

            val btnEdit:
                    ImageView =
                view.findViewById(R.id.btnEdit)

            val btnDelete:
                    ImageView =
                view.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ContactViewHolder {

            val view =
                LayoutInflater
                    .from(parent.context)

                    .inflate(
                        R.layout.item_contact,
                        parent,
                        false
                    )

            return ContactViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ContactViewHolder,
            position: Int
        ) {

            val contact =
                contacts[position]

            holder.tvAvatar.text =
                contact.contact_name
                    .firstOrNull()
                    ?.uppercase()
                    ?: "?"

            holder.tvContactName.text =
                contact.contact_name

            val detail =
                if (
                    contact.contact_relation
                        .isNotEmpty()
                ) {

                    "${contact.contact_relation} • ${contact.contact_phone}"

                } else {

                    contact.contact_phone
                }

            holder.tvContactDetail.text =
                detail

            holder.btnEdit.setOnClickListener {
                onEdit(contact)
            }

            holder.btnDelete.setOnClickListener {
                onDelete(contact)
            }
        }

        override fun getItemCount() =
            contacts.size
    }
}