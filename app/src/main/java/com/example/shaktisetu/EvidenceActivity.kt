package com.example.shaktisetu

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.io.File

class EvidenceActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var rvPhotos:
            RecyclerView

    private lateinit var rvAudio:
            RecyclerView

    private lateinit var tvNoPhotos:
            TextView

    private lateinit var tvNoAudio:
            TextView

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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            } else {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
            decorView.systemUiVisibility = flags
        }

        setContentView(
            R.layout.activity_evidence
        )

        BottomNavHelper.setup(
            this,
            "evidence"
        )

        rvPhotos =
            findViewById(R.id.rvPhotos)

        rvAudio =
            findViewById(R.id.rvAudio)

        tvNoPhotos =
            findViewById(R.id.tvNoPhotos)

        tvNoAudio =
            findViewById(R.id.tvNoAudio)

        val btnDeleteEvidence =
            findViewById<TextView>(
                R.id.btnDeleteEvidence
            )

        rvPhotos.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvAudio.layoutManager =
            LinearLayoutManager(this)

        loadEvidence()

        btnDeleteEvidence.setOnClickListener {
            confirmDeleteAllEvidence()
        }
    }

    // Load Evidence
    private fun loadEvidence() {

        val photos = getEvidencePhotos()

        if (photos.isEmpty()) {

            tvNoPhotos.visibility =
                View.VISIBLE

            rvPhotos.visibility =
                View.GONE

        } else {

            tvNoPhotos.visibility =
                View.GONE

            rvPhotos.visibility =
                View.VISIBLE

            rvPhotos.adapter =
                PhotoAdapter(photos)
        }

        val audioFiles =
            getEvidenceAudio()

        if (audioFiles.isEmpty()) {

            tvNoAudio.visibility =
                View.VISIBLE

            rvAudio.visibility =
                View.GONE

        } else {

            tvNoAudio.visibility =
                View.GONE

            rvAudio.visibility =
                View.VISIBLE

            rvAudio.adapter =
                AudioAdapter(audioFiles) { file ->
                    playAudio(file)
                }
        }
    }

    // Get Photos
    private fun getEvidencePhotos():
            List<File> {

        val photoDir =
            getExternalFilesDir(
                Environment
                    .DIRECTORY_PICTURES
            )

        return photoDir?.listFiles { file ->

            file.name.startsWith("SOS_") &&
                    file.extension == "jpg"

        }?.sortedByDescending {

            it.lastModified()

        } ?: emptyList()
    }

    // Get Audio
    private fun getEvidenceAudio():
            List<File> {

        val audioDir =
            getExternalFilesDir(
                Environment
                    .DIRECTORY_MUSIC
            )

        return audioDir?.listFiles { file ->

            file.name.startsWith("SOS_") &&
                    file.extension == "3gp"

        }?.sortedByDescending {

            it.lastModified()

        } ?: emptyList()
    }

    // Delete Confirmation
    private fun confirmDeleteAllEvidence() {

        val totalItems =
            getEvidencePhotos().size +
                    getEvidenceAudio().size

        if (totalItems == 0) {

            Toast.makeText(
                this,
                "No evidence found.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        AlertDialog.Builder(this)

            .setTitle("Delete Evidence")

            .setMessage(
                "Delete all saved evidence?\n\nThis action cannot be undone."
            )

            .setPositiveButton("Delete") { _, _ ->
                deleteAllEvidence()
            }

            .setNegativeButton(
                "Cancel",
                null
            )

            .show()
    }

    // Delete All
    private fun deleteAllEvidence() {

        mediaPlayer?.release()

        mediaPlayer = null

        var deletedCount = 0

        getEvidencePhotos().forEach { file ->

            if (file.delete()) {
                deletedCount++
            }
        }

        getEvidenceAudio().forEach { file ->

            if (file.delete()) {
                deletedCount++
            }
        }

        loadEvidence()

        if (deletedCount > 0) {

            Toast.makeText(
                this,
                "✅ Evidence deleted.",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            Toast.makeText(
                this,
                "❌ Could not delete evidence.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Play Audio
    private fun playAudio(
        file: File
    ) {

        try {

            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {

                setDataSource(
                    file.absolutePath
                )

                prepare()

                start()
            }

            Toast.makeText(
                this,
                "▶ Playing ${file.name}",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                this,
                "❌ Cannot play audio",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {

        super.onDestroy()

        mediaPlayer?.release()

        mediaPlayer = null
    }

    override fun finish() {

        super.finish()

        overridePendingTransition(
            R.anim.zoom_fade_in_back,
            R.anim.zoom_fade_out_back
        )
    }

    // =========================
    // PHOTO ADAPTER
    // =========================

    inner class PhotoAdapter(
        private val photos: List<File>
    ) : RecyclerView.Adapter<
            PhotoAdapter.PhotoViewHolder>() {

        inner class PhotoViewHolder(
            view: View
        ) : RecyclerView.ViewHolder(view) {

            val ivPhoto:
                    ImageView =
                view.findViewById(R.id.ivPhoto)

            val tvPhotoName:
                    TextView =
                view.findViewById(R.id.tvPhotoName)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PhotoViewHolder {

            val view =
                LayoutInflater.from(
                    parent.context
                ).inflate(
                    R.layout.item_photo,
                    parent,
                    false
                )

            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: PhotoViewHolder,
            position: Int
        ) {

            val file = photos[position]

            holder.ivPhoto.load(file) {

                crossfade(true)
            }

            holder.tvPhotoName.text =
                file.name

                    .removePrefix("SOS_")

                    .removeSuffix(".jpg")
        }

        override fun getItemCount() =
            photos.size
    }

    // =========================
    // AUDIO ADAPTER
    // =========================

    inner class AudioAdapter(

        private val audioFiles:
        List<File>,

        private val onPlayClick:
            (File) -> Unit

    ) : RecyclerView.Adapter<
            AudioAdapter.AudioViewHolder>() {

        inner class AudioViewHolder(
            view: View
        ) : RecyclerView.ViewHolder(view) {

            val tvAudioName:
                    TextView =
                view.findViewById(R.id.tvAudioName)

            val btnPlay:
                    TextView =
                view.findViewById(R.id.btnPlay)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AudioViewHolder {

            val view =
                LayoutInflater.from(
                    parent.context
                ).inflate(
                    R.layout.item_audio,
                    parent,
                    false
                )

            return AudioViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: AudioViewHolder,
            position: Int
        ) {

            val file =
                audioFiles[position]

            holder.tvAudioName.text =
                file.name

                    .removePrefix("SOS_")

                    .removeSuffix(".3gp")

            holder.btnPlay.setOnClickListener {
                onPlayClick(file)
            }
        }

        override fun getItemCount() =
            audioFiles.size
    }
}