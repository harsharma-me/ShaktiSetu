package com.example.shaktisetu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.shaktisetu.R
import com.example.shaktisetu.ui.theme.NavActive
import com.example.shaktisetu.ui.theme.NavInactive

@Composable
fun BottomNavigationBar(
    activeTab: String,
    onTabClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), // Reduced container height
        contentAlignment = Alignment.BottomCenter
    ) {
        // --- Floating Glassy Container ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp) // Adjusted bottom padding
                .height(64.dp)
                .shadow(
                    elevation = 25.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.92f),
                            Color(0xFFFDEFF2).copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.2f))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NavItem(
                    iconRes = R.drawable.eye_scan_svgrepo_com,
                    isActive = activeTab == "evidence",
                    onClick = { onTabClick("evidence") }
                )
                NavItem(
                    iconRes = R.drawable.ic_nav_contacts,
                    isActive = activeTab == "contacts",
                    onClick = { onTabClick("contacts") }
                )
                
                // Gap for central FAB
                Spacer(modifier = Modifier.width(64.dp))

                NavItem(
                    iconRes = R.drawable.ic_nav_call,
                    isActive = activeTab == "call",
                    onClick = { onTabClick("call") }
                )
                NavItem(
                    iconRes = R.drawable.ic_nav_settings,
                    isActive = activeTab == "settings",
                    onClick = { onTabClick("settings") }
                )
            }
        }

        // --- Premium Center Shield (SOS) ---
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "scale")

        Box(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing Glow behind the Shield
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        clip = false,
                        spotColor = Color(0xFFEA4B55).copy(alpha = 0.8f)
                    )
                    .background(Color(0xFFEA4B55).copy(alpha = 0.15f), CircleShape)
            )
            
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B), // Lighter Red
                                Color(0xFFEA4B55), // Brand Red
                                Color(0xFFB71C1C)  // Darker Red
                            )
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onTabClick("home") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glassy Reflection on top of Shield
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(0.25f), Color.Transparent),
                                endY = 100f
                            ),
                            CircleShape
                        )
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_shield),
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val color by animateColorAsState(if (isActive) NavActive else NavInactive, label = "color")
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .size(50.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
            if (isActive) {
                Spacer(modifier = Modifier.height(6.dp))
                // Soft glow dot for active state
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .shadow(4.dp, CircleShape, spotColor = color)
                        .background(color, CircleShape)
                )
            }
        }
    }
}
