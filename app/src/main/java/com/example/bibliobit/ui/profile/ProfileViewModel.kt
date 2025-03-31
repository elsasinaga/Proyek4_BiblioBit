package com.example.bibliobit.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.model.User
import com.example.bibliobit.data.repository.AppDatabase
import com.example.bibliobit.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _profileData = MutableLiveData<User>()
    val profileData: LiveData<User> get() = _profileData

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                val localUser = db.userDao().getUser(uid)
                if (localUser != null) {
                    _profileData.postValue(localUser.toUser())
                    if (!localUser.isSynced && isOnline()) {
                        syncLocalToFirestore(localUser)
                    }
                } else {
                    val userDoc = firestore.collection("users").document(uid).get().await()
                    if (userDoc.exists()) {
                        val username = userDoc.getString("username") ?: ""
                        val name = userDoc.getString("name") ?: ""
                        val profileImage = userDoc.getString("profileImage")
                        val user = User(
                            email = firebaseUser.email ?: "",
                            uid = uid,
                            username = username,
                            name = name,
                            profileImage = profileImage
                        )
                        _profileData.postValue(user)
                        saveToLocal(user, true)
                    } else {
                        val defaultUser = User(
                            email = firebaseUser.email ?: "",
                            uid = uid
                        )
                        _profileData.postValue(defaultUser)
                        saveToLocal(defaultUser, true)
                        upsertProfileToFirestore(defaultUser)
                    }
                }
            } else {
                _profileData.postValue(null)
            }
        }
    }

    fun upsertProfile(name: String, username: String) {
        val currentUser = _profileData.value ?: return
        val updatedUser = currentUser.copy(name = name, username = username)
        _profileData.value = updatedUser
        viewModelScope.launch {
            val localUser = updatedUser.toLocalUser(isOnline())
            db.userDao().upsert(localUser)
            if (isOnline()) {
                upsertProfileToFirestore(updatedUser)
            }
        }
    }

    fun updateProfileImage(imagePath: String) {
        val currentUser = _profileData.value ?: return
        val updatedUser = currentUser.copy(profileImage = imagePath)
        _profileData.value = updatedUser
        viewModelScope.launch {
            val localUser = updatedUser.toLocalUser(isOnline())
            db.userDao().upsert(localUser)
            if (isOnline()) {
                upsertProfileToFirestore(updatedUser)
            }
        }
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

    private fun upsertProfileToFirestore(user: User) {
        val userData = hashMapOf(
            "email" to user.email,
            "username" to user.username,
            "name" to user.name,
            "profileImage" to user.profileImage
        )
        firestore.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                viewModelScope.launch {
                    db.userDao().upsert(user.toLocalUser(true))
                }
            }
    }

    private fun syncLocalToFirestore(localUser: LocalUser) {
        val userData = hashMapOf(
            "email" to localUser.email,
            "username" to localUser.username,
            "name" to localUser.name,
            "profileImage" to localUser.profileImage
        )
        firestore.collection("users").document(localUser.uid)
            .set(userData)
            .addOnSuccessListener {
                viewModelScope.launch {
                    db.userDao().upsert(localUser.copy(isSynced = true))
                }
            }
    }

    private fun saveToLocal(user: User, isSynced: Boolean) {
        viewModelScope.launch {
            db.userDao().upsert(user.toLocalUser(isSynced))
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        return network != null
    }

    private fun User.toLocalUser(isSynced: Boolean) = LocalUser(
        uid = uid,
        email = email,
        username = username,
        name = name,
        profileImage = profileImage,
        isSynced = isSynced
    )

    private fun LocalUser.toUser() = User(
        email = email,
        uid = uid,
        username = username,
        name = name,
        profileImage = profileImage
    )
}