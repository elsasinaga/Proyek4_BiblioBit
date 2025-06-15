package com.example.bibliobit // <- PASTIKAN NAMA PACKAGE INI SESUAI DENGAN PROYEK ANDA

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Metode ini dipanggil ketika ada pesan masuk SAAT APLIKASI SEDANG DIBUKA (foreground).
     * Notifikasi di background ditangani otomatis oleh sistem.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // INI BAGIAN YANG DIPERBAIKI
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Cek jika pesan memiliki data payload (opsional)
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
        }

        // Cek jika pesan memiliki notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Title: ${it.title}")
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // Di sini Anda bisa membuat notifikasi custom jika ingin tampilan berbeda
            // saat aplikasi di foreground.
        }
    }

    /**
     * Metode ini dipanggil setiap kali token registrasi FCM yang baru dibuat.
     * Token ini adalah alamat unik untuk setiap instalasi aplikasi di perangkat.
     * Anda HARUS mengirim token ini ke server backend Anda agar bisa mengirim notifikasi
     * ke perangkat tertentu.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Di sinilah Anda biasanya mengirim token ke server Anda (misalnya via Retrofit)
        // sendRegistrationToServer(token)
    }

    companion object {
        private const val TAG = "FCM_SERVICE" // Tag untuk memfilter log
    }
}