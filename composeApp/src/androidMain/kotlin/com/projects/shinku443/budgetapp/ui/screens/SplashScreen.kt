package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Delay navigation after splash
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB9E2FE)), // Icy blue
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Penguin mascot image
            Image(
                painter = painterResource(R.drawable.splash_screen_logo),
                contentDescription = "Emperor Wallet Logo",
                modifier = Modifier.size(240.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
//            AnimatedCoins()
//
            AnimatedCoinsRow()
//            Text(
//                text = "EMPEROR",
//                fontSize = 32.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black
//            )
//
//            Box(
//                modifier = Modifier
//                    .background(Color.Black, shape = RoundedCornerShape(8.dp))
//                    .padding(horizontal = 16.dp, vertical = 4.dp)
//            ) {
//                Text(
//                    text = "WALLET",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//            }
        }
    }
}

@Composable
fun AnimatedCoins() {
    val coinCount = 3
    val delays = listOf(0, 300, 600) // staggered timing

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement
            .spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterHorizontally
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(coinCount) { index ->
            val offsetY by animateDpAsState(
                targetValue = if (index == 0) (-40).dp else (-60 - index * 10).dp,
                animationSpec = tween(durationMillis = 800, delayMillis = delays[index])
            )

            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, delayMillis = delays[index])
            )

            Icon(
                painter = painterResource(R.drawable.ic_coins),
                contentDescription = "Coin",
                modifier = Modifier
                    .size(48.dp)
                    .offset(y = offsetY)
                    .alpha(alpha),
            )
        }
    }
}

@Composable
fun AnimatedCoin(delayMillis: Int, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(durationMillis = 600)
        ) + fadeIn(animationSpec = tween(600)),
        exit = fadeOut()
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_coins),
            contentDescription = "Coin",
            modifier = modifier.size(32.dp),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun AnimatedCoinsRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        AnimatedCoin(delayMillis = 0)
        AnimatedCoin(delayMillis = 300)
        AnimatedCoin(delayMillis = 600)
    }
}


@Preview
@Composable
fun SplashScreenPreview(modifier: Modifier = Modifier) {
    SplashScreen( {})

}
