package com.example.bibliobit.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bibliobit.ui.HomeScreen
import com.example.bibliobit.ui.addbook.AddBookScreen
import com.example.bibliobit.ui.addbook.AddBookViewModel
import com.example.bibliobit.ui.bookdetail.BookDetailScreen
import com.example.bibliobit.ui.bookdetail.BookDetailViewModel
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordScreen
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordViewModel
import com.example.bibliobit.ui.library.LibraryScreen
import com.example.bibliobit.ui.library.LibraryViewModel
import com.example.bibliobit.ui.login.LoginScreen
import com.example.bibliobit.ui.login.LoginViewModel
import com.example.bibliobit.ui.onboarding.OnboardingScreen
import com.example.bibliobit.ui.profile.ProfileScreen
import com.example.bibliobit.ui.register.RegisterScreen
import com.example.bibliobit.ui.register.RegisterViewModel
import com.example.bibliobit.ui.readingprogress.AddReadingProgressScreen
import com.example.bibliobit.ui.readingprogress.ReadingProgressViewModel
import com.example.bibliobit.ui.readingprogress.YourProgressReadingScreen
import com.example.bibliobit.ui.readingprogress.YourReadingBookScreen
import com.example.bibliobit.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Add : Screen("add")
    object Statistic : Screen("statistic")
    object Library : Screen("library")
    object Profile : Screen("profile")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    object YourReadingBook : Screen("your_reading_book/{userId}/{bookId}") {
        fun createRoute(userId: String, bookId: Long) = "your_reading_book/$userId/$bookId"
    }
    object AddReadingProgress : Screen("add_reading_progress/{userLibraryId}/{bookTitle}/{totalPages}/{userId}/{bookId}") {
        fun createRoute(userLibraryId: Long, bookTitle: String, totalPages: Int, userId: String, bookId: Long) =
            "add_reading_progress/$userLibraryId/$bookTitle/$totalPages/$userId/$bookId"
    }
    object YourProgressReading : Screen("your_progress_reading/{userLibraryId}/{bookTitle}/{totalPages}") {
        fun createRoute(userLibraryId: Long, bookTitle: String, totalPages: Int) =
            "your_progress_reading/$userLibraryId/$bookTitle/$totalPages"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()

    val isOnboardingCompleted = runBlocking {
        preferencesManager.isOnboardingCompletedFlow.first()
    }

    val startDestination = when {
        auth.currentUser != null && isOnboardingCompleted -> Screen.Home.route
        isOnboardingCompleted -> Screen.Login.route
        else -> Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            var isOnboardingFinished by remember { mutableStateOf(false) }

            LaunchedEffect(isOnboardingFinished) {
                if (isOnboardingFinished) {
                    preferencesManager.setOnboardingCompleted(true)
                    if (auth.currentUser != null) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            }

            OnboardingScreen(
                onBoardingComplete = {
                    isOnboardingFinished = true
                }
            )
        }

        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
            ForgotPasswordScreen(
                viewModel = forgotPasswordViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen()
        }

        composable(Screen.Add.route) {
            val viewModel: AddBookViewModel = hiltViewModel()
            AddBookScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = androidx.navigation.NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            val viewModel: BookDetailViewModel = hiltViewModel()
            BookDetailScreen(
                bookId = bookId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.YourReadingBook.route,
            arguments = listOf(
                navArgument("userId") { type = androidx.navigation.NavType.StringType },
                navArgument("bookId") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            val viewModel: ReadingProgressViewModel = hiltViewModel()
            YourReadingBookScreen(
                userId = userId,
                bookId = bookId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProgress = { userLibraryId, bookTitle, totalPages, uid, bid ->
                    navController.navigate(
                        Screen.AddReadingProgress.createRoute(
                            userLibraryId = userLibraryId,
                            bookTitle = bookTitle,
                            totalPages = totalPages,
                            userId = uid,
                            bookId = bid
                        )
                    )
                },
                onNavigateToSeeProgress = { userLibraryId ->
                    val book = viewModel.book.value
                    if (book != null) {
                        navController.navigate(
                            Screen.YourProgressReading.createRoute(
                                userLibraryId = userLibraryId,
                                bookTitle = book.title,
                                totalPages = book.pages ?: 0
                            )
                        )
                    }
                }
            )
        }

        composable(
            route = Screen.AddReadingProgress.route,
            arguments = listOf(
                navArgument("userLibraryId") { type = androidx.navigation.NavType.LongType },
                navArgument("bookTitle") { type = androidx.navigation.NavType.StringType },
                navArgument("totalPages") { type = androidx.navigation.NavType.IntType },
                navArgument("userId") { type = androidx.navigation.NavType.StringType },
                navArgument("bookId") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val userLibraryId = backStackEntry.arguments?.getLong("userLibraryId") ?: 0L
            val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: ""
            val totalPages = backStackEntry.arguments?.getInt("totalPages") ?: 0
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            val viewModel: ReadingProgressViewModel = hiltViewModel()
            AddReadingProgressScreen(
                userLibraryId = userLibraryId,
                bookTitle = bookTitle,
                totalPages = totalPages,
                userId = userId,
                bookId = bookId,
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(
            route = Screen.YourProgressReading.route,
            arguments = listOf(
                navArgument("userLibraryId") { type = androidx.navigation.NavType.LongType },
                navArgument("bookTitle") { type = androidx.navigation.NavType.StringType },
                navArgument("totalPages") { type = androidx.navigation.NavType.IntType }
            )
        ) { backStackEntry ->
            val userLibraryId = backStackEntry.arguments?.getLong("userLibraryId") ?: 0L
            val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: ""
            val totalPages = backStackEntry.arguments?.getInt("totalPages") ?: 0
            val viewModel: ReadingProgressViewModel = hiltViewModel()
            YourProgressReadingScreen(
                userLibraryId = userLibraryId,
                bookTitle = bookTitle,
                totalPages = totalPages,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistic.route) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Statistic Screen")
            }
        }

        composable(Screen.Library.route) {
            val viewModel: LibraryViewModel = hiltViewModel()
            LibraryScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                modifier = Modifier,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}