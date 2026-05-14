package com.example.shaktisetu.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val contact_name: String,
    val contact_phone: String,
    val contact_relation: String
)