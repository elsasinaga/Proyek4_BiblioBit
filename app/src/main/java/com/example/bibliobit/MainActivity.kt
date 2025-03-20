package com.example.bibliobit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bibliobit.ui.login.LoginScreen
import com.example.bibliobit.ui.login.LoginViewModel
import com.example.bibliobit.ui.register.RegisterScreen
import com.example.bibliobit.ui.register.RegisterViewModel
import com.example.bibliobit.ui.theme.BiblioBitTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bibliobit.ui.HomeScreen
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordScreen
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiblioBitTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    var isLoggedIn by remember { mutableStateOf(false) }
                    var showRegisterScreen by remember { mutableStateOf(false) }
                    var showForgotPasswordScreen by remember { mutableStateOf(false) }

                    if (isLoggedIn) {
                        HomeScreen()
                    } else {
                        when {
                            showForgotPasswordScreen -> {
                                val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
                                ForgotPasswordScreen(
                                    viewModel = forgotPasswordViewModel,
                                    onNavigateBack = { showForgotPasswordScreen = false }
                                )
                            }
                            showRegisterScreen -> {
                                val registerViewModel: RegisterViewModel = hiltViewModel()
                                RegisterScreen(
                                    viewModel = registerViewModel,
                                    onRegisterSuccess = { isLoggedIn = true },
                                    onNavigateToLogin = { showRegisterScreen = false }
                                )
                            }
                            else -> {
                                val loginViewModel: LoginViewModel = hiltViewModel()
                                LoginScreen(
                                    viewModel = loginViewModel,
                                    onLoginSuccess = { isLoggedIn = true },
                                    onNavigateToRegister = { showRegisterScreen = true },
                                    onNavigateToForgotPassword = { showForgotPasswordScreen = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BiblioBitTheme {
        Greeting("Android")
    }
}