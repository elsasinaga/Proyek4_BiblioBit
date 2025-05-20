package com.example.bibliobit

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.work.*
import androidx.navigation.compose.rememberNavController
import com.example.bibliobit.ui.MainScreen
import com.example.bibliobit.ui.theme.BiblioBitTheme
import com.example.bibliobit.utils.PreferencesManager
import com.example.bibliobit.utils.ReadingStreak
import com.example.bibliobit.work.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var readingStreak: ReadingStreak

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleSyncWorker()
        setContent {
            BiblioBitTheme {
                val navController = rememberNavController()
                MainScreen(
                    navController = navController,
                    preferencesManager = preferencesManager,
                    readingStreak = readingStreak
                )
            }
        }
    }

    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("syncWork", ExistingPeriodicWorkPolicy.KEEP, syncRequest)
    }
}