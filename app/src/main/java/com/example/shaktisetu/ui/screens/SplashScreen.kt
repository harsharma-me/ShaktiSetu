package com.example.shaktisetu.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shaktisetu.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val cinematicDarkBg = Color(0xFF050304)
    val cinematicRose = Color(0xFFD4A5A5)
    val cinematicAccentMuted = Color(0xFFB88B8B)
    val cinematicPinkGray = Color(0xFF8C7A80)
    val premiumGlow = Color(0xFF3D1C22).copy(alpha = 0.4f)

    val bdScript = FontFamily(Font(R.font.bdscript_regular))
    val interSemiBold = FontFamily(Font(R.font.inter_semibold))

    val scale = remember { Animatable(0.7f) }
    val alphaLogo = remember { Animatable(0f) }
    val alphaText = remember { Animatable(0f) }
    val translationYText = remember { Animatable(40f) }
    val alphaTagline = remember { Animatable(0f) }
    val translationYTagline = remember { Animatable(25f) }
    val alphaLoader = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = overshootInterpolator()
            )
        }
        launch {
            alphaLogo.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700)
            )
        }

        delay(300)
        launch {
            alphaText.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )
        }
        launch {
            translationYText.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500)
            )
        }

        delay(250) 
        launch {
            alphaTagline.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450)
            )
        }
        launch {
            translationYTagline.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 450)
            )
        }

        delay(300)
        launch {
            alphaLoader.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }

        delay(1200) 
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cinematicDarkBg)
    ) {
        Box(
            modifier = Modifier
                .size(450.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(premiumGlow, Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.download),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale.value)
                    .alpha(alphaLogo.value),
                colorFilter = ColorFilter.tint(cinematicRose)
            )

            Spacer(modifier = Modifier.height(42.dp))

            Text(
                text = "ShaktiSetu",
                fontFamily = bdScript,
                fontSize = 76.sp,
                color = cinematicAccentMuted,
                modifier = Modifier
                    .alpha(alphaText.value)
                    .offset(y = translationYText.value.dp),
                letterSpacing = 0.02.sp
            )

            Spacer(modifier = Modifier.height(0.dp))

            Text(
                text = "A bridge to safety",
                fontFamily = interSemiBold,
                fontSize = 15.sp,
                color = cinematicPinkGray,
                letterSpacing = 6.sp,
                modifier = Modifier
                    .alpha(alphaTagline.value)
                    .offset(y = translationYTagline.value.dp)
            )
        }

        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .size(44.dp)
                .alpha(alphaLoader.value),
            color = cinematicRose,
            strokeWidth = 3.dp
        )
    }
}

private fun overshootInterpolator() = tween<Float>(
    durationMillis = 700,
    easing = { OvershootInterpolator(1.3f).getInterpolation(it) }
)

class OvershootInterpolator(private val tension: Float = 2f) {
    fun getInterpolation(input: Float): Float {
        var t = input
        t -= 1.0f
        return t * t * ((tension + 1) * t + tension) + 1.0f
    }
}
