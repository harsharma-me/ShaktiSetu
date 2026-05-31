package com.example.shaktisetu.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shaktisetu.R

@Composable
fun SosAlertScreen(
    countdown: Int,
    isSosActive: Boolean,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    val bdScriptFont = FontFamily(Font(R.font.bdscript_regular))
    
    // Background Pulse Animation for SOS Active state
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val bgColor = if (isSosActive) Color(0xFF8B0000) else Color(0xFFD42A2A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isSosActive) {
                        listOf(Color(0xFF630000), bgColor)
                    } else {
                        listOf(bgColor, Color(0xFF8B0000))
                    }
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Shakti Setu",
                fontFamily = bdScriptFont,
                fontSize = 48.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 40.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Warning Icon with Pulse
            Box(contentAlignment = Alignment.Center) {
                if (isSosActive) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_warning_triangle),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSosActive) "SOS ACTIVATED" else "Alert Active",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))

            // Countdown or Active Symbol
            if (!isSosActive) {
                Text(
                    text = countdown.toString(),
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_white),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(150.dp).scale(pulseScale)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Mute Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onMuteToggle() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_speaker),
                    contentDescription = null,
                    tint = if (isMuted) Color.Gray else Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isMuted) "UNMUTE SIREN" else "MUTE SIREN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dismiss Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(bottom = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Dismiss Emergency",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
