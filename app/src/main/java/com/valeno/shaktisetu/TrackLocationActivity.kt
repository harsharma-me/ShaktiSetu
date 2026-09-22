package com.valeno.shaktisetu

import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.overlay.Marker

import org.osmdroid.views.MapView
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.valeno.shaktisetu.ui.screens.TrackLocationScreen
import com.valeno.shaktisetu.ui.theme.ShaktiSetuTheme

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackLocationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var marker: Marker? = null
    private var userUid: String? = null
    private var mapView: MapView? = null

    private val statusText = mutableStateOf("Initializing tracking...")
    private val isLoading = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load configuration in background to avoid blocking UI thread
        lifecycleScope.launch(Dispatchers.IO) {
            Configuration.getInstance().load(applicationContext, 
                PreferenceManager.getDefaultSharedPreferences(applicationContext))
        }
        
        userUid = intent.getStringExtra("user_uid") ?: intent.data?.getQueryParameter("uid")

        if (userUid == null) {
            Toast.makeText(this, "No user to track", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            ShaktiSetuTheme {
                UpdateManager.CheckForUpdates(this)
                TrackLocationScreen(
                    statusText = statusText.value,
                    isLoading = isLoading.value,
                    onMapReady = { mv ->
                        mapView = mv
                        setupMap(mv)
                    }
                )
            }
        }

        // Defer tracking start to allow activity transition to finish smoothly
        window.decorView.postDelayed({
            if (!isFinishing) {
                startTracking()
            }
        }, 500)
    }

    private fun setupMap(mv: MapView) {
        mv.setTileSource(TileSourceFactory.MAPNIK)
        mv.setMultiTouchControls(true)
        mv.controller.setZoom(17.0)
        
        marker = Marker(mv)
        marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker?.title = "Current Location"
        mv.overlays.add(marker)
    }

    private fun startTracking() {
        // Firestore listener runs on a background worker by default, 
        // but we ensure the processing is off-loaded.
        db.collection("live_sos").document(userUid!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    statusText.value = "Error tracking location"
                    return@addSnapshotListener
                }

                lifecycleScope.launch {
                    if (snapshot != null && snapshot.exists()) {
                        val geoPoint = snapshot.getGeoPoint("location")
                        if (geoPoint != null) {
                            // Process update on UI thread for map animation
                            updateMarkerSmoothly(geoPoint)
                            isLoading.value = false
                            statusText.value = "Tracking Live Location"
                        }
                    } else {
                        statusText.value = "SOS Event Ended"
                        isLoading.value = false
                    }
                }
            }
    }

    private fun updateMarkerSmoothly(geoPoint: GeoPoint) {
        val osmPoint = OsmGeoPoint(geoPoint.latitude, geoPoint.longitude)
        marker?.position = osmPoint
        // Use animation for a cinematic, jitter-free movement
        mapView?.controller?.animateTo(osmPoint)
        mapView?.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
}
