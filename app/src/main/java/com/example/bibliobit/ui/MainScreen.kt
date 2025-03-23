package com.example.bibliobit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bibliobit.R
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordScreen
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordViewModel
import com.example.bibliobit.ui.login.LoginScreen
import com.example.bibliobit.ui.login.LoginViewModel
import com.example.bibliobit.ui.onboarding.OnboardingScreen
import com.example.bibliobit.ui.register.RegisterScreen
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
                    painter = painterResource(id = R.drawable.logo), // Ganti dengan logo Anda
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
        val startDestination = if (isOnboardingCompleted) "register" else "onboarding"

        NavHost(navController = navController, startDestination = startDestination) {
            composable("onboarding") {
                var isOnboardingFinished by remember { mutableStateOf(false) }

                LaunchedEffect(isOnboardingFinished) {
                    if (isOnboardingFinished) {
                        preferencesManager.setOnboardingCompleted(true)
                        navController.navigate("register") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }

                OnboardingScreen(
                    onBoardingComplete = {
                        isOnboardingFinished = true
                    }
                )
            }
            composable("login") {
                val loginViewModel: LoginViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") }
                )
            }
            composable("register") {
                val registerViewModel: RegisterViewModel = hiltViewModel()
                RegisterScreen(
                    viewModel = registerViewModel,
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }
            composable("forgot_password") {
                val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
                ForgotPasswordScreen(
                    viewModel = forgotPasswordViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("home") {
                HomeScreen()
            }
        }
    }
}