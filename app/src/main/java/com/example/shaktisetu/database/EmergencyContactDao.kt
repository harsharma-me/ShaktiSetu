package com.example.shaktisetu.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.shaktisetu.models.EmergencyContact

@Dao
interface EmergencyContactDao {

    @Insert
    suspend fun insertContact(
        contact: EmergencyContact
    )

    @Update
    suspend fun updateContact(
        contact: EmergencyContact
    )

    @Query("SELECT * FROM emergency_contacts")
    suspend fun getAllContacts():
            List<EmergencyContact>
    @Delete
    suspend fun deleteContact(
        contact: EmergencyContact
    )
}