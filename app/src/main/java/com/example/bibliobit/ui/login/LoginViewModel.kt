package com.example.bibliobit.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.repository.AuthRepository // ## DIPERBAIKI: Dependensi ke Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    // ## DIPERBAIKI: Hanya butuh AuthRepository
    private val authRepository: AuthRepository
) : ViewModel() {
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var emailError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail
        emailError = null
        errorMessage = null
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        passwordError = null
        errorMessage = null
    }

    private fun validateInput(): Boolean {
        var isValid = true
        if (email.isBlank()) {
            emailError = "Email tidak boleh kosong"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Masukkan email yang valid"
            isValid = false
        }
        if (password.isBlank()) {
            passwordError = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password harus minimal 6 karakter"
            isValid = false
        }
        return isValid
    }

    fun login(onLoginSuccess: () -> Unit) {
        if (!validateInput()) {
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // ## DIPERBAIKI: Panggil fungsi login dari repository
                val result = authRepository.login(email, password)

                result.onSuccess {
                    // Jika login di repository berhasil, panggil callback
                    onLoginSuccess()
                }
                result.onFailure {
                    // Jika gagal, tampilkan pesan error dari repository
                    errorMessage = it.message ?: "Email atau password tidak sesuai"
                }

            } catch (e: Exception) {
                errorMessage = "Terjadi kesalahan: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}