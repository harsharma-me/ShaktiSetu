package com.valeno.shaktisetu

import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.valeno.shaktisetu.models.AppUpdateInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object UpdateManager {

    @Composable
    fun CheckForUpdates(context: Context) {
        var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
        var showDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val db = FirebaseFirestore.getInstance()
            db.collection("app_updates")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val info = snapshot.documents[0].toObject(AppUpdateInfo::class.java)
                        if (info != null) {
                            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                            val currentVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)

                            if (info.versionCode > currentVersionCode) {
                                updateInfo = info
                                showDialog = true
                            }
                        }
                    }
                }
        }

        if (showDialog && updateInfo != null) {
            UpdateDialog(
                info = updateInfo!!,
                onDismiss = { if (!updateInfo!!.forceUpdate) showDialog = false },
                onUpdate = {
                    val intent = Intent(Intent.ACTION_VIEW, updateInfo!!.updateUrl.toUri())
                    context.startActivity(intent)
                }
            )
        }
    }

    @Composable
    private fun UpdateDialog(
        info: AppUpdateInfo,
        onDismiss: () -> Unit,
        onUpdate: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "New Update Available: ${info.versionName}") },
            text = {
                Text(text = if (info.updateNotes.isNotEmpty()) info.updateNotes else "A new version of ShaktiSetu is available. Please update to continue using the app with latest features and security.")
            },
            confirmButton = {
                Button(onClick = onUpdate) {
                    Text("Update Now")
                }
            },
            dismissButton = {
                if (!info.forceUpdate) {
                    TextButton(onClick = onDismiss) {
                        Text("Later")
                    }
                }
            }
        )
    }
}
