package com.example.bibliobit.ui.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.components.Button1 // Assuming Button1 is in this package
import com.example.bibliobit.ui.theme.abu1 // Assuming abu1 is in this package
import com.example.bibliobit.ui.theme.abu2 // Assuming abu2 is in this package
import com.example.bibliobit.ui.theme.abu3 // Assuming abu3 is in this package
import com.example.bibliobit.ui.theme.merah // Assuming merah is in this package
import com.example.bibliobit.ui.theme.putih // Assuming putih is in this package
import java.io.File // Used for profileImagePath when it's a file path

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    navController: NavController,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Tangani navigasi setelah logout
    LaunchedEffect(uiState.shouldNavigateToLogin) {
        if (uiState.shouldNavigateToLogin) {
            Log.d("ProfileScreen", "Navigating to login screen")
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
            viewModel.resetNavigation()
        }
    }

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
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
    var name by remember(user.name) { mutableStateOf(user.name ?: "") }
    var username by remember(user.username) { mutableStateOf(user.username ?: "") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImagePath by remember(user.profileImage) { mutableStateOf(user.profileImage ?: "") }

    // Launcher untuk memilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Gambar Profil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(putih, CircleShape) // Added background from target style
                    .clickable(enabled = isEditing) {
                        imagePickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center // Center content within the Box
            ) {
                if (profileImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize() // Use fillMaxSize for the image within the Box
                            .clip(CircleShape) // Ensure image is also clipped to circle
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile Image",
                        modifier = Modifier.size(50.dp), // Adjusted size from target style
                        tint = abu1 // Adjusted tint from target style
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Adjusted height

            if (isEditing) {
                // Upload Photo Button
                Button1( // Changed to Button1
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(0.35f),
                ) {
                    Text(
                        text = "Upload Photo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth() // Removed horizontal padding from here as it's in the parent Box
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth() // Removed horizontal padding from here
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = user.email ?: "No email", // Email is not editable in the target style
                    onValueChange = { /* Do nothing, email is not editable */ },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false // Email is disabled as per target style
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center, // Centered arrangement
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button1( // Changed to Button1
                        onClick = { isEditing = false }, // Cancel button just exits editing mode
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .padding(end = 8.dp),
                        backgroundColor = abu2 // Color from target style
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Button1( // Changed to Button1
                        onClick = {
                            onUpdateProfile(name, username)
                            isEditing = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.4f) // Adjusted fillMaxWidth
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else {
                // Nama Pengguna
                Text(
                    text = user.name ?: "Unknown",
                    style = MaterialTheme.typography.titleLarge, // Adjusted style
                    color = abu3 // Adjusted color
                )

                // Username (commented out as per the target style)
                // Text(
                //     text = "@${user.username ?: "unknown"}",
                //     style = MaterialTheme.typography.bodyMedium,
                //     color = MaterialTheme.colorScheme.onSurfaceVariant
                // )

                // Email
                Text(
                    text = user.email ?: "No email",
                    style = MaterialTheme.typography.bodyLarge, // Adjusted style
                    color = abu2 // Adjusted color
                )

                Spacer(modifier = Modifier.height(16.dp)) // Adjusted height

                // Edit Profile Button
                Button1( // Changed to Button1
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth(0.35f) // Adjusted fillMaxWidth
                ) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.labelSmall, // Adjusted style
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Adjusted height

                // Tombol Logout
                Button1( // Changed to Button1
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(0.3f), // Adjusted fillMaxWidth
                    backgroundColor = merah // Adjusted background color
                ) {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.labelSmall, // Adjusted style
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}