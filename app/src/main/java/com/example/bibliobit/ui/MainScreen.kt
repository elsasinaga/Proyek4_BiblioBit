package com.example.bibliobit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    val isOnboardingCompleted by preferencesManager.isOnboardingCompletedFlow.collectAsState(initial = false)
    val startDestination = if (isOnboardingCompleted) "register" else "onboarding" // Ubah dari "login" ke "register"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            var isOnboardingFinished by remember { mutableStateOf(false) }

            LaunchedEffect(isOnboardingFinished) {
                if (isOnboardingFinished) {
                    preferencesManager.setOnboardingCompleted(true)
                    navController.navigate("register") { // Ubah dari "login" ke "register"
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
                onNavigateToLogin = { navController.popBackStack() }
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