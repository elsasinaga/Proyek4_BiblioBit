package com.example.bibliobit.ui.profile

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream

data class ProfileData(
    val id: Int = 0,
    val nama: String,
    val email: String,
    val username: String,
    val profileImage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _dataProfile = MutableLiveData<ProfileData>()
    val dataProfile: LiveData<ProfileData> get() = _dataProfile

    init {
        // Data awal (contoh)
        _dataProfile.value = ProfileData(
            id = 1,
            nama = "John Doe",
            email = "john.doe@example.com",
            username = "johndoe",
            profileImage = null
        )
    }

    fun upsertProfile(id: Int, nama: String, username: String, email: String) {
        _dataProfile.value = ProfileData(
            id = id,
            nama = nama,
            email = email,
            username = username,
            profileImage = _dataProfile.value?.profileImage
        )
    }

    fun updateProfileImage(imagePath: String) {
        _dataProfile.value = _dataProfile.value?.copy(profileImage = imagePath)
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri): String {
        val file = File(context.filesDir, "profile_image_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}