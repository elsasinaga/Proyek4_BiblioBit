package com.example.bibliobit.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.repository.AuthRepository
import com.example.bibliobit.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Definisikan satu state untuk merepresentasikan semua kondisi UI
data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: LocalUser? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchUserData()
    }

    fun fetchUserData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true) // Mulai loading
            try {
                // Langsung ambil profil dari backend Laravel kita
                val userProfile = userRepository.getProfile()
                _uiState.value = ProfileUiState(isLoading = false, user = userProfile)
            } catch (e: Exception) {
                // Jika ada error, update state dengan pesan error
                _uiState.value = ProfileUiState(isLoading = false, error = "Failed to load profile: ${e.message}")
            }
        }
    }

    fun updateProfile(name: String, username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val updatedProfile = userRepository.updateProfile(name, username)
                _uiState.value = _uiState.value.copy(isLoading = false, user = updatedProfile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to update profile: ${e.message}")
            }
        }
    }

    // Fungsi logout tidak berubah
    fun logout() {
        authRepository.logout()
    }
}