package com.example.bibliobit.ui.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail.trim()
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = authRepository.login(email, password)
            isLoading = false
            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                errorMessage = exception.message
            }
        }
    }
}