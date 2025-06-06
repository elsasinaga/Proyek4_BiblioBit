package com.example.bibliobit.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val user = firebaseAuth.currentUser
        Log.d("AuthInterceptor", "Current User: ${user?.uid ?: "null"}")

        if (user != null) {
            val token = try {
                runBlocking(Dispatchers.IO) { // Tetap gunakan runBlocking untuk kompatibilitas, tapi batasi penggunaannya
                    user.getIdToken(true).await()?.token
                }
            } catch (e: Exception) {
                Log.e("AuthInterceptor", "Token retrieval failed", e)
                null
            }
            Log.d("AuthInterceptor", "Retrieved token: $token")
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                Log.w("AuthInterceptor", "No valid token available, proceeding without authentication")
            }
        } else {
            Log.w("AuthInterceptor", "No user logged in, proceeding without authentication")
        }

        val newRequest = requestBuilder.build()
        Log.d("AuthInterceptor", "Request URL: ${newRequest.url}") // Log URL untuk debugging
        return chain.proceed(newRequest)
    }
}
