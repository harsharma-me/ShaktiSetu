package com.example.shaktisetu

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.shaktisetu.ui.screens.EvidenceScreen
import java.io.File

class EvidenceActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateUi()
    }

    private fun updateUi() {
        val photos = getEvidencePhotos()
        val audioFiles = getEvidenceAudio()

        setContent {
            EvidenceScreen(
                photos = photos,
                audioFiles = audioFiles,
                onPlayAudio = { file -> playAudio(file) },
                onDeleteAll = { confirmDeleteAllEvidence() },
                onTabClick = { tab ->
                    BottomNavHelper.handleTabClick(this, tab)
                }
            )
        }
    }

    private fun getEvidencePhotos(): List<File> {
        val photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return photoDir?.listFiles { file ->
            file.name.startsWith("SOS_") && file.extension == "jpg"
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun getEvidenceAudio(): List<File> {
        val audioDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return audioDir?.listFiles { file ->
            file.name.startsWith("SOS_") && file.extension == "3gp"
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun confirmDeleteAllEvidence() {
        val totalItems = getEvidencePhotos().size + getEvidenceAudio().size
        if (totalItems == 0) {
            Toast.makeText(this, "No evidence found.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Evidence")
            .setMessage("Delete all saved evidence?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteAllEvidence() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAllEvidence() {
        mediaPlayer?.release()
        mediaPlayer = null
        getEvidencePhotos().forEach { it.delete() }
        getEvidenceAudio().forEach { it.delete() }
        updateUi()
        Toast.makeText(this, "✅ Evidence deleted.", Toast.LENGTH_SHORT).show()
    }

    private fun playAudio(file: File) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
            Toast.makeText(this, "▶ Playing ${file.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Cannot play audio", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}