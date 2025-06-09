package com.example.bibliobit.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit
) {
    // ## DIPERBAIKI: Observe satu uiState utama ##
    val uiState by viewModel.uiState.collectAsState()

    when {
        // Tampilkan loading indicator jika isLoading true
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        // Tampilkan pesan error jika ada
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
        // Tampilkan konten utama jika user tidak null
        uiState.user != null -> {
            val user = uiState.user!!
            ProfileContent(
                modifier = modifier,
                user = user,
                onUpdateProfile = { name, username ->
                    viewModel.updateProfile(name, username)
                },
                onLogout = {
                    viewModel.logout()
                    onNavigateToLogin()
                }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    user: com.example.bibliobit.data.model.LocalUser,
    onUpdateProfile: (name: String, username: String) -> Unit,
    onLogout: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    // State untuk form edit, diinisialisasi dari data user
    var name by remember(user.name) { mutableStateOf(user.name ?: "") }
    var username by remember(user.username) { mutableStateOf(user.username ?: "") }
    var profileImagePath by remember(user.profileImage) { mutableStateOf(user.profileImage ?: "") }

    // ... (Logika image picker Anda bisa diletakkan di sini)

    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            // ... (Kode UI untuk menampilkan foto, nama, email, tombol, dll.)
            // ... (Pastikan Anda mengambil data dari parameter `user`, contoh: `user.name`, `user.email`)
            // ... (Saat tombol save diklik, panggil `onUpdateProfile(name, username)`)
            // ... (Saat tombol logout diklik, panggil `onLogout()`)

            // Contoh Tombol Save
            // Button(onClick = { onUpdateProfile(name, username); isEditing = false }) { Text("Save") }
        }
    }
}