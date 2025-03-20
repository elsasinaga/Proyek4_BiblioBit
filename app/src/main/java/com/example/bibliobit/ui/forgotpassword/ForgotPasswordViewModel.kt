package com.example.bibliobit.ui.forgotpassword

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
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail.trim()
    }

    fun resetPassword() {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(email)) {
            errorMessage = "Please enter a valid email"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            val result = authRepository.resetPassword(email)
            isLoading = false
            result.onSuccess {
                successMessage = "Reset email sent. Check your inbox!"
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to send reset email"
            }
        }
    }
}