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

import org.osmdroid.views.MapView
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.example.shaktisetu.ui.screens.TrackLocationScreen
import com.example.shaktisetu.ui.theme.ShaktiSetuTheme

class TrackLocationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var marker: Marker? = null
    private var userUid: String? = null
    private var mapView: MapView? = null

    private val statusText = mutableStateOf("Initializing tracking...")
    private val isLoading = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        
        userUid = intent.getStringExtra("user_uid") ?: intent.data?.getQueryParameter("uid")

        if (userUid == null) {
            Toast.makeText(this, "No user to track", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            ShaktiSetuTheme {
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

        startTracking()
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
        db.collection("live_sos").document(userUid!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    statusText.value = "Error tracking location"
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val geoPoint = snapshot.getGeoPoint("location")
                    if (geoPoint != null) {
                        updateMarker(geoPoint)
                        isLoading.value = false
                        statusText.value = "Tracking Live Location"
                    }
                } else {
                    statusText.value = "SOS Event Ended"
                    isLoading.value = false
                }
            }
    }

    private fun updateMarker(geoPoint: GeoPoint) {
        val osmPoint = OsmGeoPoint(geoPoint.latitude, geoPoint.longitude)
        marker?.position = osmPoint
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
