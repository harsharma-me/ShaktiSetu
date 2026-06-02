package com.example.shaktisetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shaktisetu.R

@Composable
fun IncomingCallScreen(
    callerName: String,
    phoneNumber: String,
    onDecline: () -> Unit,
    onAccept: () -> Unit,
    onMessage: () -> Unit
) {
    val darkBg = Color(0xFF050304)
    val primaryRed = Color(0xFFC94C63)
    val glassWhite = Color(0xFFFFFFFF).copy(alpha = 0.2f)
    val glassStroke = Color(0xFFFFFFFF).copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "INCOMING CALL",
                color = Color(0xFFE0E0E0),
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = callerName,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = phoneNumber,
                color = Color(0xFFA8B5AA),
                fontSize = 16.sp
            )
        }

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(40.dp),
                color = Color(0xFFFFFFFF).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, glassStroke)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onDecline() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Decline",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 87.dp, height = 64.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF4CAF50))
                            .clickable { onAccept() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📞", fontSize = 26.sp, color = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onAccept() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Answer",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
