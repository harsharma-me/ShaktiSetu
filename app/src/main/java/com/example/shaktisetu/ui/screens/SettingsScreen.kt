package com.example.shaktisetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.shaktisetu.R
import com.example.shaktisetu.ui.components.BottomNavigationBar
import com.example.shaktisetu.ui.theme.BgEnd
import com.example.shaktisetu.ui.theme.BgStart
import com.example.shaktisetu.ui.theme.BrandText

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
    Scaffold(
        bottomBar = {
            BottomNavigationBar(activeTab = "settings", onTabClick = onTabClick)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgStart, BgEnd)))
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
                        color = BrandText
                    )
                }

                // Profile Progress Card
                if (profileProgress < 100) {
                    item {
                        ProfileProgressCard(profileProgress, onClick = { onActionClick("profile") })
                    }
                }

                item {
                    SettingsSectionTitle("Safety Controls")
                }

                item {
                    ToggleSettingItem(
                        title = "Shake Detection",
                        subtitle = "Trigger SOS by shaking your phone",
                        icon = R.drawable.ic_shield,
                        isChecked = isShakeEnabled,
                        onCheckedChange = onShakeToggle
                    )
                }

                item {
                    ToggleSettingItem(
                        title = "Voice Command",
                        subtitle = "Help! Trigger SOS with voice",
                        icon = R.drawable.ic_speaker,
                        isChecked = isVoiceEnabled,
                        onCheckedChange = onVoiceToggle
                    )
                }

                item {
                    SettingsSectionTitle("Account & Security")
                }

                item {
                    ActionSettingItem(
                        title = "My Profile",
                        icon = R.drawable.ic_nav_profile,
                        onClick = { onActionClick("profile") }
                    )
                }

                item {
                    ActionSettingItem(
                        title = "Change SOS PIN",
                        icon = R.drawable.ic_password,
                        onClick = { onActionClick("change_pin") }
                    )
                }

                item {
                    ActionSettingItem(
                        title = "Reset Password",
                        icon = R.drawable.ic_mail,
                        onClick = { onActionClick("reset_password") }
                    )
                }

                item {
                    SettingsSectionTitle("Information")
                }

                item {
                    ActionSettingItem(
                        title = "Terms & Conditions",
                        icon = R.drawable.ic_shield_white,
                        onClick = { onActionClick("terms") }
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
fun ProfileProgressCard(progress: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandText.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.size(50.dp),
                    color = BrandText,
                    strokeWidth = 4.dp,
                    trackColor = BrandText.copy(alpha = 0.1f)
                )
                Text(text = "$progress%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandText)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Complete your profile", fontWeight = FontWeight.Bold, color = BrandText)
                Text(text = "Better safety with more info", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = BrandText.copy(alpha = 0.6f),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun ToggleSettingItem(
    title: String,
    subtitle: String,
    icon: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandText)
        )
    }
}

@Composable
fun ActionSettingItem(title: String, icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_back_arrow), // Reuse as forward arrow
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp).rotate(180f)
        )
    }
}

@Composable
fun IconBox(icon: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(BrandText.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = BrandText,
            modifier = Modifier.size(20.dp)
        )
    }
}
