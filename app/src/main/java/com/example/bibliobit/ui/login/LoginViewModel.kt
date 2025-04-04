package com.example.bibliobit.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.repository.UserDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth, // Injeksi FirebaseAuth menggunakan Hilt
    private val userDao: UserDao, // Tambahkan injeksi UserDao
    private val firestore: FirebaseFirestore // Tambahkan injeksi Firestore
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
            try {
                isLoading = true
                errorMessage = null

                // Autentikasi menggunakan Firebase
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    // Ambil data pengguna dari Firestore
                    val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val username = userDoc.getString("username") ?: ""
                    val name = userDoc.getString("name") ?: ""
                    val profileImage = userDoc.getString("profileImage")

                    val localUser = LocalUser(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        username = username,
                        name = name,
                        profileImage = profileImage,
                        isSynced = true
                    )
                    userDao.upsert(localUser)
                    onLoginSuccess()
                } ?: run {
                    errorMessage = "Email atau password tidak sesuai"
                }
            } catch (e: Exception) {
                errorMessage = "Terjadi kesalahan: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}