package com.example.bibliobit.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.theme.abu1
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.abu3
import com.example.bibliobit.ui.theme.merah
import com.example.bibliobit.ui.theme.putih
import java.io.File

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val profileData by viewModel.profileData.observeAsState()
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(profileData?.name ?: "") }
    var email by remember { mutableStateOf(profileData?.email ?: "") }
    var username by remember { mutableStateOf(profileData?.username ?: "") }
    var profileImagePath by remember { mutableStateOf(profileData?.profileImage ?: "") }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val imagePath = viewModel.saveImageToInternalStorage(context, it)
            profileImagePath = imagePath
            viewModel.updateProfileImage(imagePath)
        }
    }

    LaunchedEffect(profileData) {
        profileData?.let { profile ->
            name = profile.name
            email = profile.email
            username = profile.username
            profileImagePath = profile.profileImage ?: ""
        }
    }

    // Tampilkan loading jika profileData belum ada
    if (profileData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(putih, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profileImagePath.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(File(profileImagePath)),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile Picture",
                        tint = abu1,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                Button1(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(0.35f),
                ) {
                    Text(
                        text = "Upload Photo",
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button1(
                    onClick = {
                        viewModel.upsertProfile(name, username)
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth(0.3f)
                ) {
                    Text(
                        text = "Save",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            } else {
                Text(text = name, style = MaterialTheme.typography.titleLarge, color = abu3)
//                Text(text = username, style = MaterialTheme.typography.bodyLarge)
                Text(text = email, style = MaterialTheme.typography.bodyLarge, color = abu2)
                Spacer(modifier = Modifier.height(16.dp))
                Button1(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth(0.35f)
                ) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button1(
                    onClick = {
                        viewModel.logout()
                        onNavigateToLogin()
                    },
                    modifier = Modifier.fillMaxWidth(0.3f),
                    backgroundColor = merah
                ) {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}