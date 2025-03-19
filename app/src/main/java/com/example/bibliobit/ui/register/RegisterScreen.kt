package com.example.bibliobit.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bibliobit.R
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.Label
import com.example.bibliobit.ui.theme.hitam

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val username = viewModel.username
    val email = viewModel.email
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimary)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gambar di bagian atas
        Image(
            painter = painterResource(id = R.drawable.register),
            contentDescription = "Register Illustration",
            modifier = Modifier
                .size(260.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "BiblioBit",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Field Username
        Label(
            label = "Username",
            value = username,
            onValueChange = { viewModel.onUsernameChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .padding(bottom = 16.dp)
        )

        // Field Email
        Label(
            label = "Email",
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .padding(bottom = 16.dp)
        )

        // Field Password
        Label(
            label = "Password",
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .padding(bottom = 16.dp)
        )

        // Field Confirm Password
        Label(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .padding(bottom = 16.dp)
        )

        Button1(
            onClick = { viewModel.register(onRegisterSuccess) },
            modifier = Modifier
                .fillMaxWidth(0.3f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onNavigateToLogin
        ) {
            Row {
                Text(
                    text = "Sudah memiliki akun? ",
                    style = MaterialTheme.typography.labelSmall,
                    color = hitam
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = hitam
                )
            }
        }

        // Pesan error
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