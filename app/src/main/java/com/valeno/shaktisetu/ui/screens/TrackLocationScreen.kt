package com.valeno.shaktisetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView

import com.valeno.shaktisetu.ui.theme.BrandText
import com.valeno.shaktisetu.ui.theme.SosRed

@Composable
fun TrackLocationScreen(
    statusText: String,
    isLoading: Boolean,
    onMapReady: (MapView) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    onMapReady(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = SosRed
            )
        }

        Text(
            text = statusText,
            color = BrandText,
            modifier = Modifier
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(8.dp)
                .align(Alignment.TopStart)
        )
    }
}
