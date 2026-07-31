package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.model.WalletTransactionEntity
import com.example.data.repository.*
import com.example.ui.screens.*
import com.example.ui.theme.LuckySpinTheme
import com.example.ui.viewmodel.*
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Database & Repositories
        val database = AppDatabase.getInstance(this)

        val authRepository = AuthRepository(database.userDao(), database.gameSettingsDao(), database.auditLogDao())
        val gameRepository = GameRepository(
            database.gameRoomDao(),
            database.betDao(),
            database.userDao(),
            database.roundHistoryDao(),
            database.globalJackpotDao(),
            database.gameSettingsDao(),
            database.walletTransactionDao(),
            database.auditLogDao()
        )
        val adminRepository = AdminRepository(
            database.userDao(),
            database.advertisementDao(),
            database.gameSettingsDao(),
            database.globalJackpotDao(),
            database.supportTicketDao(),
            database.auditLogDao(),
            database.walletTransactionDao()
        )

        // ViewModels
        val authViewModel = AuthViewModel(authRepository)
        val gameViewModel = GameViewModel(gameRepository)
        val adminViewModel = AdminViewModel(adminRepository)
        val supportViewModel = SupportViewModel(database.supportTicketDao())

        setContent {
            LuckySpinTheme {
                val navController = rememberNavController()

                val currentUser by authViewModel.currentUser.collectAsState()
                val jackpot by adminRepository.globalJackpot.collectAsState(initial = null)
                val ads by adminRepository.allAds.collectAsState(initial = emptyList())
                val transactions by database.walletTransactionDao().getTransactionsForUser(currentUser?.id ?: "").collectAsState(initial = emptyList())
                val userBets by gameRepository.observeUserBets(currentUser?.id ?: "").collectAsState(initial = emptyList())

                val startDestination = if (currentUser != null) "dashboard" else "auth"

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("auth") {
                            AuthScreen(
                                authViewModel = authViewModel,
                                onAuthSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("dashboard") {
                            DashboardScreen(
                                user = currentUser,
                                globalJackpot = jackpot,
                                ads = ads,
                                onJoinRoomClick = { navController.navigate("game_room") },
                                onWalletClick = { navController.navigate("wallet") },
                                onProfileClick = { navController.navigate("profile") },
                                onSupportClick = { navController.navigate("support") },
                                onAdminClick = { navController.navigate("admin") }
                            )
                        }

                        composable("game_room") {
                            GameRoomScreen(
                                user = currentUser,
                                gameViewModel = gameViewModel,
                                onBackClick = { navController.popBackStack() },
                                onWalletClick = { navController.navigate("wallet") },
                                onProfileClick = { navController.navigate("profile") },
                                onAdminClick = { navController.navigate("admin") }
                            )
                        }

                        composable("wallet") {
                            WalletScreen(
                                user = currentUser,
                                transactions = transactions,
                                onDepositRequest = { amount ->
                                    currentUser?.let { user ->
                                        adminViewModel.adjustUserCoins(user.email, user.id, 0L, amount)
                                    }
                                },
                                onWithdrawRequest = { amount ->
                                    currentUser?.let { user ->
                                        if (user.realCoins >= amount) {
                                            adminViewModel.adjustUserCoins(user.email, user.id, 0L, -amount)
                                        }
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("support") {
                            SupportChatScreen(
                                user = currentUser,
                                supportViewModel = supportViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("admin") {
                            AdminPanelScreen(
                                currentUser = currentUser,
                                adminViewModel = adminViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("profile") {
                            ProfileHistoryScreen(
                                user = currentUser,
                                userBets = userBets,
                                onLogoutClick = {
                                    authViewModel.logout()
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
