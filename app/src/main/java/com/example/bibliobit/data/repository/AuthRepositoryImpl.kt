package com.example.bibliobit.data.repository
import com.example.bibliobit.data.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Result.success(User(email = firebaseUser.email ?: "", uid = firebaseUser.uid))
            } else {
                Result.failure(Exception("Login failed: User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun register(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Result.success(User(email = firebaseUser.email ?: "", uid = firebaseUser.uid))
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