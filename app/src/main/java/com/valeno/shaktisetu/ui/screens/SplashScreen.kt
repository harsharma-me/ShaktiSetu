package com.valeno.shaktisetu.ui.screens

import com.valeno.shaktisetu.R

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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeno.shaktisetu.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val bgStart = BgStart
    val bgEnd = BgEnd
    val brandText = BrandText
    val navActive = NavActive
    val navInactive = NavInactive

    val bdScript = FontFamily(Font(R.font.bdscript_regular))
    val interSemiBold = FontFamily(Font(R.font.inter_semibold))

    // Animation States
    val scale = remember { Animatable(0.7f) }
    val alphaLogo = remember { Animatable(0f) }
    val alphaText = remember { Animatable(0f) }
    val translationYText = remember { Animatable(40f) }
    val alphaTagline = remember { Animatable(0f) }
    val translationYTagline = remember { Animatable(25f) }
    val alphaLoader = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo Animation
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

        // App Name Animation
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

        // Tagline Animation
        delay(250) // Relative to App Name start
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

        // Loader Animation
        delay(300)
        launch {
            alphaLoader.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }

        delay(1200) // Total delay to match 2200ms approx
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
    ) {
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
                colorFilter = ColorFilter.tint(brandText)
            )

            Spacer(modifier = Modifier.height(42.dp))

            Text(
                text = "ShaktiSetu",
                fontFamily = bdScript,
                fontSize = 76.sp,
                color = brandText,
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
                color = navInactive,
                letterSpacing = 6.sp,
                modifier = Modifier
                    .alpha(alphaTagline.value)
                    .offset(y = translationYTagline.value.dp)
            )
        }

        // Loader
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .size(44.dp)
                .alpha(alphaLoader.value),
            color = navActive,
            strokeWidth = 3.dp
        )
    }
}

private fun overshootInterpolator() = tween<Float>(
    durationMillis = 700,
    easing = { OvershootInterpolator(1.3f).getInterpolation(it) }
)

// Helper class for OvershootInterpolator in Compose
class OvershootInterpolator(private val tension: Float = 2f) {
    fun getInterpolation(input: Float): Float {
        var t = input
        t -= 1.0f
        return t * t * ((tension + 1) * t + tension) + 1.0f
    }
}
