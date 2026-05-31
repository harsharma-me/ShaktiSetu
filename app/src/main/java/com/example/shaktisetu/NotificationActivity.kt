package com.example.shaktisetu

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateListOf
import com.example.shaktisetu.ui.screens.NotificationScreen
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class NotificationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val notificationsList = mutableStateListOf<NotificationModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        BottomNavHelper.setup(this, "home")

        setContent {
            ShaktiSetuTheme {
                NotificationScreen(
                    notifications = notificationsList,
                    onBackClick = { finish() }
                )
            }
        }

        fetchNotifications()
    }

    private fun fetchNotifications() {
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                notificationsList.clear()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val title = doc.getString("title") ?: "Alert"
                        val body = doc.getString("body") ?: ""
                        val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date()
                        notificationsList.add(NotificationModel(title, body, timestamp))
                    }
                }
            }
    }

    data class NotificationModel(val title: String, val body: String, val timestamp: Date)
}
