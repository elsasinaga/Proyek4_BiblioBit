package com.example.bibliobit

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.bibliobit.data.remote.RemoteDataSource
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiblioBitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    MainScreen(
                        navController = navController,
                        preferencesManager = preferencesManager,
                        readingStreak = readingStreak,
                        remoteDataSource = remoteDataSource
                    )
                }
            }
        }
    }
}