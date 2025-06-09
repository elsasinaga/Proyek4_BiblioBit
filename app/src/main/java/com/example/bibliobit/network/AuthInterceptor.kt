package com.example.bibliobit.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val user = firebaseAuth.currentUser

        // Jika tidak ada user, lanjutkan request tanpa token
        if (user == null) {
            Log.w("AuthInterceptor", "No user logged in, proceeding without Authorization header.")
            return chain.proceed(originalRequest)
        }

        // Jika ada user, coba ambil token
        val token = try {
            // runBlocking diperlukan di sini karena interceptor bersifat sinkron
            runBlocking {
                user.getIdToken(false).await()?.token
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Failed to retrieve Firebase token", e)
            null
        }

        // Jika token tidak berhasil didapat, lanjutkan tanpa token
        if (token == null) {
            Log.w("AuthInterceptor", "Token is null, proceeding without Authorization header.")
            return chain.proceed(originalRequest)
        }

        // Tambahkan header Authorization jika token berhasil didapat
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        Log.d("AuthInterceptor", "Request to ${newRequest.url} with Authorization header.")
        return chain.proceed(newRequest)
    }
}