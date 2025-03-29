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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bibliobit.R
import com.example.bibliobit.ui.components.BottomNavigationBar
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordViewModel
import com.example.bibliobit.ui.login.LoginViewModel
import com.example.bibliobit.ui.navigation.AppNavHost
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.register.RegisterViewModel
import com.example.bibliobit.utils.PreferencesManager

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    preferencesManager: PreferencesManager
) {
    var isLoading by remember { mutableStateOf(true) }
    var isOnboardingCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        preferencesManager.isOnboardingCompletedFlow.collect { completed ->
            isOnboardingCompleted = completed
            isLoading = false
        }
    }

    if (isLoading) {
        // Animasi untuk layar loading
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animasi Fade-In untuk logo
                var logoAlpha by remember { mutableStateOf(0f) }
                val alphaAnimation by animateFloatAsState(
                    targetValue = logoAlpha,
                    animationSpec = tween(
                        durationMillis = 1000, // Durasi animasi fade-in
                        easing = LinearEasing
                    )
                )

                LaunchedEffect(Unit) {
                    logoAlpha = 1f // Mulai animasi fade-in
                }

                // Animasi Pulsasi untuk logo
                val scaleAnimation by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1200,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Image(
                    painter = painterResource(id = R.drawable.logo_bibliobit), // Ganti dengan logo Anda
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .alpha(alphaAnimation) // Animasi fade-in
                        .scale(scaleAnimation) // Animasi pulsasi
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Animasi Rotasi untuk indikator loading
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1000, // Durasi satu putaran
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )

                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(rotation), // Animasi rotasi
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        // Gunakan rute dari Screen sealed class untuk startDestination
        val startDestination = if (isOnboardingCompleted) Screen.Register.route else Screen.Onboarding.route

        // Ambil rute saat ini dengan penanganan null safety
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Tentukan rute-rute di mana navigation bar TIDAK ditampilkan
        val routesWithoutBottomBar = listOf(
            Screen.Onboarding.route,
            Screen.Login.route,
            Screen.Register.route,
            Screen.ForgotPassword.route
        )

        Scaffold(
            bottomBar = {
                if (currentRoute != null && currentRoute !in routesWithoutBottomBar) {
                    BottomNavigationBar(
                        navController = navController,
                        currentRoute = currentRoute
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavHost(
                    navController = navController,
                    preferencesManager = preferencesManager
                )
            }
        }
    }
}