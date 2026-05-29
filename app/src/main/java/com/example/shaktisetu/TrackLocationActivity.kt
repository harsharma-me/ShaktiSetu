package com.example.shaktisetu

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shaktisetu.databinding.ActivityTrackLocationBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.overlay.Marker

class TrackLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackLocationBinding
    private val db = FirebaseFirestore.getInstance()
    private var marker: Marker? = null
    private var userUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OSMDroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        
        binding = ActivityTrackLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userUid = intent.getStringExtra("user_uid") ?: intent.data?.getQueryParameter("uid")

        if (userUid == null) {
            Toast.makeText(this, "No user to track", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupMap()
        startTracking()
    }

    private fun setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(17.0)
        
        marker = Marker(binding.mapView)
        marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker?.title = "Current Location"
        binding.mapView.overlays.add(marker)
    }

    private fun startTracking() {
        db.collection("live_sos").document(userUid!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    binding.statusText.text = "Error tracking location"
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val geoPoint = snapshot.getGeoPoint("location")
                    if (geoPoint != null) {
                        updateMarker(geoPoint)
                        binding.progressBar.visibility = View.GONE
                        binding.statusText.text = "Tracking Live Location"
                    }
                } else {
                    binding.statusText.text = "SOS Event Ended"
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

    private fun updateMarker(geoPoint: GeoPoint) {
        val osmPoint = OsmGeoPoint(geoPoint.latitude, geoPoint.longitude)
        marker?.position = osmPoint
        binding.mapView.controller.animateTo(osmPoint)
        binding.mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
}