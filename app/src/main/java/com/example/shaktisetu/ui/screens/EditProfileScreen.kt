package com.example.shaktisetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shaktisetu.R
import com.example.shaktisetu.ui.theme.*

@Composable
fun EditProfileScreen(
    initialName: String,
    initialEmail: String,
    initialPhone: String,
    initialAddress: String,
    isUpdating: Boolean,
    onBackClick: () -> Unit,
    onUpdateClick: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var address by remember { mutableStateOf(initialAddress) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgStart, BgEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = "Back",
                        tint = BrandText
                    )
                }
                Text(
                    text = "Edit Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandText
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form
            EditField(label = "Full Name", value = name, onValueChange = { name = it })
            Spacer(modifier = Modifier.height(20.dp))
            EditField(label = "Email Address", value = initialEmail, onValueChange = {}, enabled = false)
            Spacer(modifier = Modifier.height(20.dp))
            EditField(label = "Phone Number", value = phone, onValueChange = { phone = it })
            Spacer(modifier = Modifier.height(20.dp))
            EditField(label = "Address", value = address, onValueChange = { address = it }, isMultiline = true)

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { onUpdateClick(name, phone, address) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandText),
                shape = RoundedCornerShape(16.dp),
                enabled = !isUpdating && name.isNotBlank() && phone.isNotBlank()
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Update Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    isMultiline: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BrandText.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            minLines = if (isMultiline) 3 else 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandText,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = Color.White.copy(alpha = 0.6f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                disabledContainerColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}
