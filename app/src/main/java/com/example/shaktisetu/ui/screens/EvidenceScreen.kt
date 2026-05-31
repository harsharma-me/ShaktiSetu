package com.example.shaktisetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shaktisetu.R
import com.example.shaktisetu.ui.components.BottomNavigationBar
import com.example.shaktisetu.ui.theme.BgEnd
import com.example.shaktisetu.ui.theme.BgStart
import com.example.shaktisetu.ui.theme.BrandText
import java.io.File

@Composable
fun EvidenceScreen(
    photos: List<File>,
    audioFiles: List<File>,
    onPlayAudio: (File) -> Unit,
    onDeleteAll: () -> Unit,
    onTabClick: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(activeTab = "evidence", onTabClick = onTabClick)
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
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Safety Evidence",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandText
                    )
                    
                    IconButton(onClick = onDeleteAll) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_contact), // Reusing delete icon
                            contentDescription = "Delete All",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Photos Section
                Text(
                    text = "Captured Photos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (photos.isEmpty()) {
                    EmptyEvidencePlaceholder("No photos captured yet")
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(photos) { file ->
                            PhotoEvidenceCard(file)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Audio Section
                Text(
                    text = "Audio Recordings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (audioFiles.isEmpty()) {
                    EmptyEvidencePlaceholder("No audio recordings found")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(audioFiles) { file ->
                            AudioEvidenceCard(file, onPlayAudio)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoEvidenceCard(file: File) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = file,
            contentDescription = null,
            modifier = Modifier
                .size(140.dp, 180.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = file.name.removePrefix("SOS_").removeSuffix(".jpg"),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun AudioEvidenceCard(file: File, onPlay: (File) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.6f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onPlay(file) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = BrandText.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_speaker),
                contentDescription = null,
                tint = BrandText,
                modifier = Modifier.padding(10.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name.removePrefix("SOS_").removeSuffix(".3gp"),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = "Voice Recording",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        Icon(
            painter = painterResource(id = R.drawable.ic_location_arrow), // Reusing as play icon
            contentDescription = null,
            tint = BrandText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EmptyEvidencePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.Gray, fontSize = 14.sp)
    }
}
