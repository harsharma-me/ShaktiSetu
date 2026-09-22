package com.valeno.shaktisetu

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.valeno.shaktisetu.ui.screens.EvidenceScreen
import java.io.File

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EvidenceActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val photoState = mutableStateOf<List<File>>(emptyList())
    private val audioState = mutableStateOf<List<File>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            com.valeno.shaktisetu.ui.theme.ShaktiSetuTheme {
                UpdateManager.CheckForUpdates(this)
                EvidenceScreen(
                    photos = photoState.value,
                    audioFiles = audioState.value,
                    onPlayAudio = { file -> playAudio(file) },
                    onDeleteAll = { confirmDeleteAllEvidence() },
                    onTabClick = { tab ->
                        BottomNavHelper.handleTabClick(this, tab)
                    }
                )
            }
        }

        loadEvidenceAsync()
    }

    private fun loadEvidenceAsync() {
        lifecycleScope.launch {
            val photos = withContext(Dispatchers.IO) { getEvidencePhotos() }
            val audio = withContext(Dispatchers.IO) { getEvidenceAudio() }
            photoState.value = photos
            audioState.value = audio
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
        val totalItems = photoState.value.size + audioState.value.size
        if (totalItems == 0) {
            Toast.makeText(this, "No evidence found.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Evidence")
            .setMessage("Delete all saved evidence?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        photoState.value.forEach { it.delete() }
                        audioState.value.forEach { it.delete() }
                    }
                    loadEvidenceAsync()
                    Toast.makeText(this@EvidenceActivity, "✅ Evidence deleted.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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