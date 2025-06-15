package com.example.bibliobit

// TAMBAHKAN BANYAK IMPORT BARU INI
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.bibliobit.data.remote.RemoteDataSource
import com.example.bibliobit.data.repository.AuthRepository
import com.example.bibliobit.ui.MainScreen
import com.example.bibliobit.ui.theme.BiblioBitTheme
import com.example.bibliobit.utils.PreferencesManager
import com.example.bibliobit.utils.ReadingStreak
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var remoteDataSource: RemoteDataSource
    @Inject
    lateinit var preferencesManager: PreferencesManager
    @Inject
    lateinit var readingStreak: ReadingStreak
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel() // Fungsi ini tetap ada

        setContent {
            BiblioBitTheme {
                // Launcher untuk meminta izin notifikasi
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        Log.d("Permission", "Izin notifikasi DIBERIKAN")
                    } else {
                        Log.d("Permission", "Izin notifikasi DITOLAK")
                    }
                }

                // Cek dan minta izin saat composable pertama kali dijalankan
                LaunchedEffect(key1 = true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    MainScreen(
                        navController = navController,
                        preferencesManager = preferencesManager,
                        readingStreak = readingStreak,
                        authRepository = authRepository
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = "Notifikasi Umum"
            val channelDescription = "Channel untuk notifikasi umum dan pengingat"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}