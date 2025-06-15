// File: ui/navigation/AppNavHost.kt

package com.example.bibliobit.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bibliobit.data.repository.AuthRepository
import com.example.bibliobit.ui.HomeScreen
import com.example.bibliobit.ui.addbook.AddBookScreen
import com.example.bibliobit.ui.addbook.AddBookViewModel
import com.example.bibliobit.ui.bookdetail.BookDetailScreen
import com.example.bibliobit.ui.bookdetail.BookDetailViewModel
import com.example.bibliobit.ui.components.AppScaffold
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordScreen
import com.example.bibliobit.ui.forgotpassword.ForgotPasswordViewModel
import com.example.bibliobit.ui.library.LibraryScreen
import com.example.bibliobit.ui.library.LibraryViewModel
import com.example.bibliobit.ui.login.LoginScreen
import com.example.bibliobit.ui.login.LoginViewModel
import com.example.bibliobit.ui.notes.NotesScreen
import com.example.bibliobit.ui.notes.NotesViewModel
import com.example.bibliobit.ui.onboarding.OnboardingScreen
import com.example.bibliobit.ui.profile.ProfileScreen
import com.example.bibliobit.ui.register.RegisterScreen
import com.example.bibliobit.ui.register.RegisterViewModel
import com.example.bibliobit.ui.readingprogress.AddReadingProgressScreen
import com.example.bibliobit.ui.readingprogress.ReadingProgressViewModel
import com.example.bibliobit.ui.readingprogress.YourProgressReadingScreen
import com.example.bibliobit.ui.readingprogress.YourReadingBookScreen
import com.example.bibliobit.ui.statistic.StatisticScreen
import com.example.bibliobit.ui.statistic.StatisticViewModel
import com.example.bibliobit.ui.yourfinishbook.AddYourRatingScreen
import com.example.bibliobit.ui.yourfinishbook.AddYourRatingViewModel
import com.example.bibliobit.ui.yourfinishbook.YourFinishBookScreen
import com.example.bibliobit.ui.yourfinishbook.YourFinishBookViewModel
import com.example.bibliobit.ui.yourwishlistbook.YourWishlistBookScreen
import com.example.bibliobit.ui.yourwishlistbook.YourWishlistBookViewModel
import com.example.bibliobit.utils.PreferencesManager
import com.example.bibliobit.utils.ReadingStreak
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Sealed class Screen (tidak berubah)
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
    object YourReadingBook : Screen("your_reading_book/{userLibraryId}") {
        fun createRoute(userLibraryId: Long) = "your_reading_book/$userLibraryId"
    }
    object AddReadingProgress : Screen("add_reading_progress/{userLibraryId}/{bookTitle}/{totalPages}") {
        fun createRoute(userLibraryId: Long, bookTitle: String, totalPages: Int) =
            "add_reading_progress/$userLibraryId/$bookTitle/$totalPages"
    }
    object YourProgressReading : Screen("your_progress_reading/{userLibraryId}/{bookTitle}/{totalPages}") {
        fun createRoute(userLibraryId: Long, bookTitle: String, totalPages: Int) =
            "your_progress_reading/$userLibraryId/$bookTitle/$totalPages"
    }
    object YourWishlistBook : Screen("your_wishlist_book/{bookId}") {
        fun createRoute(bookId: Long) = "your_wishlist_book/$bookId"
    }
    object YourFinishBook : Screen("your_finish_book/{bookId}") {
        fun createRoute(bookId: Long) = "your_finish_book/$bookId"
    }
    object AddYourRating : Screen("add_your_rating/{userId}/{bookId}/{bookTitle}") {
        fun createRoute(userId: String, bookId: Long, bookTitle: String) = "add_your_rating/$userId/$bookId/$bookTitle"
    }
    object Notes : Screen("notes/{userLibraryId}/{bookTitle}") {
        fun createRoute(userLibraryId: Long, bookTitle: String) = "notes/$userLibraryId/$bookTitle"
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier,
    readingStreak: ReadingStreak,
    authRepository: AuthRepository
) {
    val auth = FirebaseAuth.getInstance()
    val isOnboardingCompleted = runBlocking { preferencesManager.isOnboardingCompletedFlow.first() }
    val isUserLoggedIn = authRepository.isUserLoggedIn()

    val startDestination = when {
        isUserLoggedIn && isOnboardingCompleted -> Screen.Home.route
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
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AppScaffold(
                navController = navController,
                title = "Onboarding",
                showTopBar = false,
                showBottomBar = false
            ) { contentModifier ->
                OnboardingScreen(onBoardingComplete = { isOnboardingFinished = true })
            }
        }

        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            AppScaffold(
                navController = navController,
                title = "Login",
                showTopBar = false,
                showBottomBar = false
            ) { contentModifier ->
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }
        }

        composable(Screen.Register.route) {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            AppScaffold(
                navController = navController,
                title = "Register",
                showTopBar = false,
                showBottomBar = false
            ) { contentModifier ->
                RegisterScreen(
                    viewModel = registerViewModel,
                    onRegisterSuccess = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } } },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } } }
                )
            }
        }

        composable(Screen.ForgotPassword.route) {
            val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
            AppScaffold(
                navController = navController,
                title = "Forgot Password",
                showTopBar = false,
                showBackButton = true
            ) { contentModifier ->
                ForgotPasswordScreen(
                    viewModel = forgotPasswordViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Home.route) {
            AppScaffold(navController = navController, title = "Home") { contentModifier ->
                HomeScreen(
                    modifier = contentModifier,
                    readingStreak = readingStreak,
                    onNavigateToReadingBook = { userLibraryId ->
                        navController.navigate(Screen.YourReadingBook.createRoute(userLibraryId))
                    }
                )
            }
        }

        composable(Screen.Add.route) {
            val viewModel: AddBookViewModel = hiltViewModel()
            AppScaffold(navController = navController, title = "Add Book") { contentModifier ->
                AddBookScreen(
                    modifier = contentModifier,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            val viewModel: BookDetailViewModel = hiltViewModel()
            AppScaffold(navController = navController, title = "Book Details", showBackButton = true) { contentModifier ->
                BookDetailScreen(
                    modifier = contentModifier, // <-- TETAP PERTAHANKAN INI
                    bookId = bookId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.YourReadingBook.route,
            arguments = listOf(navArgument("userLibraryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userLibraryId = backStackEntry.arguments?.getLong("userLibraryId") ?: 0L
            val viewModel: ReadingProgressViewModel = hiltViewModel()

            YourReadingBookScreen(
                userLibraryId = userLibraryId,
                viewModel = viewModel,
                onNavigateToAddProgress = { id, title, pages ->
                    navController.navigate(
                        Screen.AddReadingProgress.createRoute(id, title, pages)
                    )
                },
                onNavigateToSeeProgress = { id, title, pages ->
                    navController.navigate(
                        Screen.YourProgressReading.createRoute(id, title, pages)
                    )
                },
                onNavigateToNotes = { id, title ->
                    navController.navigate(
                        Screen.Notes.createRoute(id, title)
                    )
                }
            )
        }

        composable(
            route = Screen.AddReadingProgress.route,
            arguments = listOf(
                navArgument("userLibraryId") { type = NavType.LongType },
                navArgument("bookTitle") { type = NavType.StringType },
                navArgument("totalPages") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userLibraryId = backStackEntry.arguments?.getLong("userLibraryId") ?: 0L
            val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: "No Title"
            val totalPages = backStackEntry.arguments?.getInt("totalPages") ?: 0
            val viewModel: ReadingProgressViewModel = hiltViewModel()

            AddReadingProgressScreen(
                userLibraryId = userLibraryId,
                bookTitle = bookTitle,
                totalPages = totalPages,
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(
            route = Screen.YourFinishBook.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            val viewModel: YourFinishBookViewModel = hiltViewModel()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            AppScaffold(navController = navController, title = "Finished Book", showBackButton = true) {
                YourFinishBookScreen(
                    userId = userId,
                    bookId = bookId,
                    viewModel = viewModel,
                    navController = navController,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.AddYourRating.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("bookId") { type = NavType.LongType },
                navArgument("bookTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: "No Title"
            val viewModel: AddYourRatingViewModel = hiltViewModel()

            AddYourRatingScreen(
                userId = userId,
                bookId = bookId,
                bookTitle = bookTitle,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistic.route) {
            val viewModel: StatisticViewModel = hiltViewModel()
            AppScaffold(navController = navController, title = "Statistic") { contentModifier ->
                StatisticScreen(
                    modifier = contentModifier,
                    viewModel = viewModel
                )
            }
        }

        composable(Screen.Library.route) {
            val viewModel: LibraryViewModel = hiltViewModel()
            AppScaffold(navController = navController, title = "Library") { contentModifier ->
                LibraryScreen(
                    modifier = contentModifier,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }

        composable(Screen.Profile.route) {
            AppScaffold(navController = navController, title = "Profile") { contentModifier ->
                ProfileScreen(
                    modifier = contentModifier,
                    navController = navController,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Screen.Notes.route,
            arguments = listOf(
                navArgument("userLibraryId") { type = NavType.LongType },
                navArgument("bookTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userLibraryId = backStackEntry.arguments?.getLong("userLibraryId") ?: 0L
            val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: "No Title"
            val viewModel: NotesViewModel = hiltViewModel()
            AppScaffold(
                navController = navController,
                title = "Notes",
                showBackButton = true,
                showBottomBar = false
            ) { contentModifier ->
                NotesScreen(
                    modifier = contentModifier,
                    userLibraryId = userLibraryId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    bookTitle = bookTitle
                )
            }
        }
    }
}