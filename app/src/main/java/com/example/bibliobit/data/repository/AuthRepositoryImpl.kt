package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Login failed: User not found in Firebase.")

            // Cukup kembalikan data user dari Firebase Auth
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email!!,
                name = firebaseUser.displayName.toString()
            )
            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            // 1. Buat user di Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Registration failed: User not created.")

            // 2. Update profil Firebase Auth dengan nama pengguna
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Backend Laravel akan mengambil 'name' ini dari token saat API pertama kali dipanggil.
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email!!,
                name = name
            )
            Result.success(user)

        } catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        return auth.currentUser?.let { firebaseUser ->
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email!!,
                name = firebaseUser.displayName.toString()
            )
        }
    }

    override fun logout() {
        auth.signOut()
    }
}