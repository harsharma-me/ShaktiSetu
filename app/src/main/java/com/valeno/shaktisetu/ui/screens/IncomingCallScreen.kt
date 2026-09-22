package com.valeno.shaktisetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.Brush
import com.valeno.shaktisetu.ui.theme.BgEnd
import com.valeno.shaktisetu.ui.theme.BgStart
import com.valeno.shaktisetu.ui.theme.BrandText
import com.valeno.shaktisetu.ui.theme.SosRed

@Composable
fun IncomingCallScreen(
    callerName: String,
    phoneNumber: String,
    onDecline: () -> Unit,
    onAccept: () -> Unit,
    onMessage: () -> Unit
) {
    val primaryRed = SosRed
    val glassWhite = Color.White.copy(alpha = 0.5f)
    val glassStroke = Color.Black.copy(alpha = 0.05f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BgStart, BgEnd)
                )
            )
    ) {
        // Top Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "INCOMING CALL",
                color = BrandText.copy(alpha = 0.7f),
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = callerName,
                color = BrandText,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = phoneNumber,
                color = BrandText.copy(alpha = 0.6f),
                fontSize = 16.sp
            )
        }

        // Central Avatar
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(primaryRed)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = callerName.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Message Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onMessage() },
                color = glassWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, glassStroke)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💬", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Message",
                        color = BrandText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Call Action Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(40.dp),
                color = Color.White.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, glassStroke)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onDecline() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Decline",
                            color = SosRed,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Accept FAB
                    Box(
                        modifier = Modifier
                            .size(width = 87.dp, height = 64.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF2E7D32))
                            .clickable { onAccept() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📞", fontSize = 26.sp, color = Color.White)
                    }

                    // Answer
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onAccept() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Answer",
                            color = Color(0xFF2E7D32),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
