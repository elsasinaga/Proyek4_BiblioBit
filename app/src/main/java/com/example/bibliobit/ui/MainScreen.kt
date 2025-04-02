package com.example.bibliobit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bibliobit.R
import com.example.bibliobit.ui.components.BottomNavigationBar
import com.example.bibliobit.ui.navigation.AppNavHost
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.utils.PreferencesManager
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    preferencesManager: PreferencesManager
) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000) // Simulasi loading selama 2 detik untuk menampilkan launch icon
        isLoading = false // Setelah loading selesai, ke AppNavHost
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var logoAlpha by remember { mutableStateOf(0f) }
                val alphaAnimation by animateFloatAsState(
                    targetValue = logoAlpha,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                )

                LaunchedEffect(Unit) {
                    logoAlpha = 1f
                }

                val scaleAnimation by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Image(
                    painter = painterResource(id = R.drawable.logo_hijau),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .alpha(alphaAnimation)
                        .scale(scaleAnimation)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(rotation),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val routesWithoutBottomBar = listOf(
            Screen.Onboarding.route,
            Screen.Login.route,
            Screen.Register.route,
            Screen.ForgotPassword.route
        )

        Scaffold(
            bottomBar = {
                if (currentRoute != null && currentRoute !in routesWithoutBottomBar) {
                    BottomNavigationBar(navController = navController, currentRoute = currentRoute)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavHost(navController = navController, preferencesManager = preferencesManager)
            }
        }
    }
}