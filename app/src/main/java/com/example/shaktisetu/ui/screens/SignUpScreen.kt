package com.example.shaktisetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shaktisetu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpClick: (String, String, String, String, String) -> Unit,
    onSignInClick: () -> Unit,
    onTermsClick: () -> Unit,
    onGoogleSignUpClick: () -> Unit,
    termsAgreed: Boolean,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF8FA),
            Color(0xFFFBECEF),
            Color(0xFFF4DDE3)
        )
    )

    val brandText = Color(0xFF7A3B3B)
    val inputBg = Color(0xFFFFFFFF)
    val bdScript = FontFamily(Font(R.font.bdscript_regular))
    val interSemiBold = FontFamily(Font(R.font.inter_semibold))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Logo Section
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFB6C8).copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ShaktiSetu",
                        fontFamily = bdScript,
                        fontSize = 64.sp,
                        color = brandText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.tagline),
                        fontFamily = interSemiBold,
                        fontSize = 13.sp,
                        color = brandText,
                        letterSpacing = 5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.create_account_subtitle),
                fontSize = 28.sp,
                color = Color(0xFF1F1F1F),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Input Fields
            SignUpTextField(value = name, onValueChange = { name = it }, placeholder = stringResource(R.string.full_name_hint))
            Spacer(modifier = Modifier.height(12.dp))
            SignUpTextField(value = email, onValueChange = { email = it }, placeholder = stringResource(R.string.email_hint), keyboardType = KeyboardType.Email)
            Spacer(modifier = Modifier.height(12.dp))
            SignUpTextField(value = phone, onValueChange = { phone = it }, placeholder = stringResource(R.string.phone_hint), keyboardType = KeyboardType.Phone)
            Spacer(modifier = Modifier.height(12.dp))
            SignUpTextField(value = password, onValueChange = { password = it }, placeholder = stringResource(R.string.password_hint), isPassword = true)
            Spacer(modifier = Modifier.height(12.dp))
            SignUpTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = stringResource(R.string.confirm_password_hint), isPassword = true)

            Spacer(modifier = Modifier.height(16.dp))

            // Terms
            Text(
                text = if (termsAgreed) "Terms & Conditions Agreed" else stringResource(R.string.agree_to_terms),
                color = if (termsAgreed) Color(0xFF4CAF50) else Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onTermsClick() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSignUpClick(name, email, phone, password, confirmPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC94C63)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "CREATE ACCOUNT",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = stringResource(R.string.already_have_account),
                color = brandText,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onSignInClick() }
            )

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = onGoogleSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE7DDE1))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign up with Google",
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = brandText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.94f),
        placeholder = { Text(placeholder, color = Color(0xFF8C7A80), fontSize = 14.sp) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF7A3B3B),
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color(0xFF7A3B3B)
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
    )
}
