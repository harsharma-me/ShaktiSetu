package com.example.shaktisetu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shaktisetu.ui.components.BottomNavigationBar
import com.example.shaktisetu.ui.components.QuickActionCard
import com.example.shaktisetu.ui.components.SOSPulseButton
import com.example.shaktisetu.ui.theme.BgEnd
import com.example.shaktisetu.ui.theme.BgStart
import com.example.shaktisetu.ui.theme.BrandText
import com.example.shaktisetu.ui.theme.SosRed

@Composable
fun MainScreen(
    onNotificationClick: () -> Unit,
    onSOSClick: () -> Unit,
    onAmbulanceClick: () -> Unit,
    onPoliceClick: () -> Unit,
    onWomenSafetyClick: () -> Unit,
    onFakeCallClick: () -> Unit,
    onTabClick: (String) -> Unit
) {
    val bdScriptFont = FontFamily(Font(R.font.bdscript_regular))

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavigationBar(
                activeTab = "home",
                onTabClick = onTabClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(BgStart, BgEnd)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Top Header ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shakti Setu",
                                fontFamily = bdScriptFont,
                                fontSize = 28.sp,
                                color = BrandText,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Welcome back, Stay Secure",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandText.copy(alpha = 0.6f)
                            )
                        }

                        Surface(
                            onClick = onNotificationClick,
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification_bell),
                                contentDescription = null,
                                tint = BrandText,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = buildAnnotatedString {
                            append("You’re ")
                            withStyle(style = SpanStyle(color = SosRed)) {
                                append("Safe")
                            }
                            append(" Here")
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A),
                        letterSpacing = (-0.5).sp
                    )
                }

                // --- Central SOS Button ---
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Refined background glow
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .blur(30.dp)
                            .graphicsLayer(alpha = 0.2f)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(SosRed, Color.Transparent)
                                ),
                                CircleShape
                            )
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SOSPulseButton(
                            onClick = onSOSClick,
                            modifier = Modifier.size(210.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Emergency assistance is just a tap away",
                            fontSize = 13.sp,
                            color = BrandText.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // --- Quick Help Section ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Help",
                            fontSize = 15.sp,
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

                    // Row 1
                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard(
                            title = "Ambulance",
                            iconRes = R.drawable.ic_ambulance,
                            iconBgColor = Color(0xFF9C27B0),
                            onClick = onAmbulanceClick,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        QuickActionCard(
                            title = "Police",
                            iconRes = R.drawable.ic_police,
                            iconBgColor = Color(0xFFFF9800),
                            onClick = onPoliceClick,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2
                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard(
                            title = "Women Safety",
                            iconRes = R.drawable.ic_women_safety,
                            iconBgColor = Color(0xFFE91E63),
                            onClick = onWomenSafetyClick,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        QuickActionCard(
                            title = "Fake Call",
                            iconRes = R.drawable.ic_fake_call,
                            iconBgColor = Color(0xFF2196F3),
                            onClick = onFakeCallClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
