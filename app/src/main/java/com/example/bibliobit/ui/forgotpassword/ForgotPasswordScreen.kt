package com.example.bibliobit.ui.forgotpassword

import androidx.compose.foundation.Image
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bibliobit.R
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.Label

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val email = viewModel.email
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val successMessage = viewModel.successMessage
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gambar ilustrasi (opsional, sesuaikan dengan tema)
        Image(
            painter = painterResource(id = R.drawable.buat_login), // Ganti dengan drawable yang sesuai
            contentDescription = "Forgot Password Illustration",
            modifier = Modifier
                .size(260.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Forgot Password",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Label(
            label = "Email",
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button1(
            onClick = { viewModel.resetPassword() },
            modifier = Modifier.fillMaxWidth(0.5f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    text = "Send Email",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateBack) {
            Text(
                text = "Kembali ke Login",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black
            )
        }

        // Pesan sukses atau error
        if (successMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = successMessage,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge
            )
        }
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