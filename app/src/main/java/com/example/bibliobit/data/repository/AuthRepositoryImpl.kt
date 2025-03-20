package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore // Tambahkan Firestore
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Ambil username dari Firestore
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val username = userDoc.getString("username") ?: ""
                Result.success(User(email = firebaseUser.email ?: "", uid = firebaseUser.uid, username = username))
            } else {
                Result.failure(Exception("Login failed: User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, username: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Simpan username ke Firestore
                val userData = hashMapOf(
                    "email" to email,
                    "username" to username
                )
                firestore.collection("users").document(firebaseUser.uid).set(userData).await()
                Result.success(User(email = firebaseUser.email ?: "", uid = firebaseUser.uid, username = username))
            } else {
                Result.failure(Exception("Registration failed: User not created"))
            }
        } catch (e: Exception) {
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
}