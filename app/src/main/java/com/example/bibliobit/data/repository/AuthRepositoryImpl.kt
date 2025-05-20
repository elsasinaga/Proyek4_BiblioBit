package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.model.User
import com.example.bibliobit.data.remote.RemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val remoteDataSource: RemoteDataSource
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val username = userDoc.getString("username") ?: ""
                val name = userDoc.getString("name") ?: ""
                val user = User(
                    email = firebaseUser.email ?: "",
                    uid = firebaseUser.uid,
                    username = username,
                    name = name
                )
                syncLocalUser(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed: User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, username: String, name: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val userData = hashMapOf(
                    "email" to email,
                    "username" to username,
                    "name" to name
                )
                firestore.collection("users").document(firebaseUser.uid).set(userData).await()
                val user = User(
                    email = firebaseUser.email ?: "",
                    uid = firebaseUser.uid,
                    username = username,
                    name = name
                )
                syncLocalUser(user)
                Result.success(user)
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

    override suspend fun syncLocalUser(user: User) {
        val localUser = LocalUser(
            uid = user.uid,
            email = user.email,
            username = user.username,
            name = user.name,
            profileImage = user.profileImage,
            isSynced = false
        )
        userDao.upsert(localUser)
        try {
            val unsyncedUsers = userDao.getUnsyncedUsers()
            if (unsyncedUsers.isNotEmpty()) {
                val syncedUsers = remoteDataSource.syncLocalUsers(unsyncedUsers)
                syncedUsers.forEach { user ->
                    userDao.upsert(user.copy(isSynced = true))
                }
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            }
            // Tangani error, data tetap di Room untuk retry
        } catch (e: Exception) {
            // Tangani error, data tetap di Room untuk retry
        }

        // Sinkronkan dari server
        try {
            val serverUsers = remoteDataSource.getLocalUsers()
            serverUsers.forEach { serverUser ->
                userDao.upsert(serverUser.copy(isSynced = true))
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            }
        } catch (e: Exception) {
            // Tangani error
        }
    }
}