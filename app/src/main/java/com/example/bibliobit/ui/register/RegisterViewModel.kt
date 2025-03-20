package com.example.bibliobit.ui.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var username by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onUsernameChange(newUsername: String) {
        username = newUsername.trim()
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail.trim()
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
    }

    fun register(onSuccess: () -> Unit) {
        // Validasi username
        if (username.isBlank()) {
            errorMessage = "Please enter a username"
            return
        }
        if (!username.matches("^[a-zA-Z0-9_]+$".toRegex())) {
            errorMessage = "Username can only contain letters, numbers, and underscores"
            return
        }

        // Validasi email
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(email)) {
            errorMessage = "Please enter a valid email (e.g., example@domain.com)"
            return
        }

        // Validasi password
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        // Validasi confirm password
        if (confirmPassword.isBlank()) {
            errorMessage = "Please confirm your password"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = authRepository.register(email, password, username) // Kirim username ke repository
            isLoading = false
            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                errorMessage = exception.message
            }
        }
    }
}