package com.valeno.shaktisetu.ui.screens

import com.valeno.shaktisetu.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.rotate
import com.valeno.shaktisetu.ui.components.BottomNavigationBar
import com.valeno.shaktisetu.ui.theme.*

@Composable
fun SettingsScreen(
    profileProgress: Int,
    isShakeEnabled: Boolean,
    onShakeToggle: (Boolean) -> Unit,
    isVoiceEnabled: Boolean,
    onVoiceToggle: (Boolean) -> Unit,
    onActionClick: (String) -> Unit,
    onTabClick: (String) -> Unit
) {
    val currentBgStart = BgStart
    val currentBgEnd = BgEnd
    val currentBrandText = BrandText
    val cardBg = Color.White.copy(alpha = 0.6f)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(activeTab = "settings", onTabClick = onTabClick)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(currentBgStart, currentBgEnd)))
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Settings",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentBrandText
                    )
                }

                // Profile Progress Card
                if (profileProgress < 100) {
                    item {
                        ProfileProgressCard(profileProgress, currentBrandText, onClick = { onActionClick("profile") })
                    }
                }

                item {
                    SettingsSectionTitle("Safety Controls", currentBrandText)
                }

                item {
                    ToggleSettingItem(
                        title = "Shake Detection",
                        subtitle = "Trigger SOS by shaking your phone",
                        icon = R.drawable.ic_shield,
                        isChecked = isShakeEnabled,
                        onCheckedChange = onShakeToggle,
                        brandColor = currentBrandText,
                        cardBg = cardBg
                    )
                }

                item {
                    ToggleSettingItem(
                        title = "Voice Command",
                        subtitle = "Help! Trigger SOS with voice",
                        icon = R.drawable.ic_speaker,
                        isChecked = isVoiceEnabled,
                        onCheckedChange = onVoiceToggle,
                        brandColor = currentBrandText,
                        cardBg = cardBg
                    )
                }

                item {
                    SettingsSectionTitle("Account & Security", currentBrandText)
                }

                item {
                    ActionSettingItem(
                        title = "My Profile",
                        icon = R.drawable.ic_nav_profile,
                        onClick = { onActionClick("profile") },
                        brandColor = currentBrandText,
                        cardBg = cardBg
                    )
                }

                item {
                    ActionSettingItem(
                        title = "Change SOS PIN",
                        icon = R.drawable.ic_password,
                        onClick = { onActionClick("change_pin") },
                        brandColor = currentBrandText,
                        cardBg = cardBg
                    )
                }

                item {
                    ActionSettingItem(
                        title = "Reset Password",
                        icon = R.drawable.ic_mail,
                        onClick = { onActionClick("reset_password") },
                        brandColor = currentBrandText,
                        cardBg = cardBg
                    )
                }

                item {
                    SettingsSectionTitle("Information", currentBrandText)
                }

                item {
                    ActionSettingItem(
                        title = "Terms & Conditions",
                        icon = R.drawable.ic_shield_white,
                        onClick = { onActionClick("terms") },
                        brandColor = currentBrandText,
                        cardBg = cardBg
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onActionClick("logout") },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(text = "Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun ProfileProgressCard(progress: Int, brandColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = brandColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.size(50.dp),
                    color = brandColor,
                    strokeWidth = 4.dp,
                    trackColor = brandColor.copy(alpha = 0.1f)
                )
                Text(text = "$progress%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = brandColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Complete your profile", fontWeight = FontWeight.Bold, color = brandColor)
                Text(text = "Better safety with more info", fontSize = 12.sp, color = brandColor.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String, brandColor: Color) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = brandColor.copy(alpha = 0.6f),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun ToggleSettingItem(
    title: String,
    subtitle: String,
    icon: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    brandColor: Color,
    cardBg: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon, brandColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, color = brandColor)
            Text(text = subtitle, fontSize = 12.sp, color = brandColor.copy(alpha = 0.6f))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandColor)
        )
    }
}

@Composable
fun ActionSettingItem(title: String, icon: Int, onClick: () -> Unit, brandColor: Color, cardBg: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon, brandColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontWeight = FontWeight.Medium, color = brandColor, modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_back_arrow), // Reuse as forward arrow
            contentDescription = null,
            tint = brandColor.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp).rotate(180f)
        )
    }
}

@Composable
fun IconBox(icon: Int, brandColor: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(brandColor.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = brandColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
