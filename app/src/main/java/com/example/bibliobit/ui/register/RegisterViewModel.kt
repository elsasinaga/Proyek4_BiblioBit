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
    var name by mutableStateOf("")
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

    fun onNameChange(newName: String) {
        name = newName
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
        // Validasi input tidak berubah...
        if (username.isBlank()) {
            errorMessage = "Please enter a username"
            return
        }
        if (name.isBlank()) { // Tambahkan validasi untuk nama
            errorMessage = "Please enter your name"
            return
        }
        // ... validasi lainnya ...
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // ## DIPERBAIKI: Hapus 'username' dari pemanggilan fungsi ##
            // Kita hanya mengirim 'name' yang akan menjadi displayName di Firebase.
            // Backend akan menggunakan displayName ini untuk membuat data awal.
            val result = authRepository.register(email, password, name)

            isLoading = false
            result.onSuccess {
                // Di sini Anda bisa menambahkan logika tambahan jika perlu,
                // misalnya, setelah registrasi berhasil, panggil fungsi untuk update username.
                onSuccess()
            }.onFailure { exception ->
                errorMessage = exception.message
            }
        }
    }
}