package com.example.bibliobit.network

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
        val requestBuilder = originalRequest.newBuilder()

        // Ambil user yang sedang login
        val user = firebaseAuth.currentUser

        // Jika user tersedia, ambil token Firebase dan tambahkan ke header Authorization
        if (user != null) {
            val token = runBlocking {
                try {
                    user.getIdToken(true).await().token
                } catch (e: Exception) {
                    null
                }
            }

            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
        }

        // Lanjutkan request dengan header yang sudah dimodifikasi
        return chain.proceed(requestBuilder.build())
    }
}
