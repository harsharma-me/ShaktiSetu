package com.example.shaktisetu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.shaktisetu.R
import com.example.shaktisetu.ui.components.BottomNavigationBar
import com.example.shaktisetu.ui.theme.BgEnd
import com.example.shaktisetu.ui.theme.BgStart
import com.example.shaktisetu.ui.theme.BrandText

@Composable
fun FakeCallScreen(
    callerName: String,
    phoneNumber: String,
    selectedDelay: Long,
    onCallerNameClick: () -> Unit,
    onDelaySelect: (Long) -> Unit,
    onScheduleClick: () -> Unit,
    onTabClick: (String) -> Unit
) {
    val bdScriptFont = FontFamily(Font(R.font.bdscript_regular))
    
    val delays = listOf(
        "Instant" to 0L,
        "5s" to 5000L,
        "15s" to 15000L,
        "30s" to 30000L,
        "1m" to 60000L,
        "2m" to 120000L
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(activeTab = "call", onTabClick = onTabClick)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgStart, BgEnd)))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "Fake Call",
                    fontFamily = bdScriptFont,
                    fontSize = 32.sp,
                    color = BrandText
                )
                
                Text(
                    text = "Discretely escape uncomfortable situations",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandText.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onCallerNameClick() },
                    color = Color.White.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(BrandText.copy(alpha = 0.1f))
                                .border(2.dp, BrandText.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_profile_placeholder),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = BrandText
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        Text(
                            text = callerName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandText
                        )

                        Text(
                            text = phoneNumber,
                            fontSize = 15.sp,
                            color = BrandText.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandText.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Edit Caller Info",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Delay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.LightGray.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(delays) { (label, delay) ->
                        val isSelected = selectedDelay == delay
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onDelaySelect(delay) },
                            color = if (isSelected) BrandText else Color.White.copy(alpha = 0.5f),
                            border = if (!isSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)) else null
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else BrandText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onScheduleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandText),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fake_call),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (selectedDelay == 0L) "Trigger Instant Call" else "Schedule Fake Call",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "A realistic call screen will be shown",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandText.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        }
    }
}
