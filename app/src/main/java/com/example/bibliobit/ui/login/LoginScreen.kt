package com.example.bibliobit.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bibliobit.R
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.Label
import androidx.compose.foundation.Image

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val email = viewModel.email
    val password = viewModel.password
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(16.dp)
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus() // Hilangkan fokus saat area kosong ditekan
                })
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.buat_login),
            contentDescription = "Login Illustration",
            modifier = Modifier
                .size(260.dp) // Atur ukuran gambar (sesuaikan sesuai kebutuhan)
                .padding(bottom = 16.dp) // Jarak antara gambar dan teks
        )
        Text(
            text = "Hello Again!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Label(
            label = "Email",
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 26.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Label(
            label = "Password",
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 26.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button1(
            onClick = { viewModel.login(onLoginSuccess) },
            modifier = Modifier.fillMaxWidth(0.3f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Tambahkan teks "Lupa Password"
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lupa Password?",
            style = MaterialTheme.typography.labelSmall
        )

        // Tambahkan teks "Register"
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Belum punya akun? Register",
            style = MaterialTheme.typography.labelSmall )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}