package com.example.expensemanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.expensemanager.ui.components.BottomNavBar
import com.example.expensemanager.ui.screens.AddEditExpenseScreen
import com.example.expensemanager.ui.screens.CreateJoinRoomScreen
import com.example.expensemanager.ui.screens.DashboardScreen
import com.example.expensemanager.ui.screens.ExpensesScreen
import com.example.expensemanager.ui.screens.ForgotPasswordScreen
import com.example.expensemanager.ui.screens.LoginScreen
import com.example.expensemanager.ui.screens.MembersScreen
import com.example.expensemanager.ui.screens.ProfileScreen
import com.example.expensemanager.ui.screens.ReportsScreen
import com.example.expensemanager.ui.screens.SettlementsScreen
import com.example.expensemanager.ui.screens.SignUpScreen
import com.example.expensemanager.ui.screens.SplashScreen
import com.example.expensemanager.viewmodel.AuthUiState
import com.example.expensemanager.viewmodel.AuthViewModel
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.RoomViewModel
import com.example.expensemanager.viewmodel.SettlementViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(),
    settlementViewModel: SettlementViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authState by authViewModel.uiState.collectAsState()
    val currentRoom by roomViewModel.currentRoom.collectAsState()
    val members by roomViewModel.members.collectAsState()

    // Auto-redirect to Login when user logs out
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Unauthenticated) {
            val route = navController.currentBackStackEntry?.destination?.route
            if (route != null &&
                route != Screen.Login.route &&
                route != Screen.SignUp.route &&
                route != Screen.Splash.route &&
                route != Screen.ForgotPassword.route
            ) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar && currentRoom != null) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    authViewModel = authViewModel,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onSignUpSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onBackToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.CreateRoom.route) {
                CreateJoinRoomScreen(
                    isCreateMode = true,
                    roomViewModel = roomViewModel,
                    authViewModel = authViewModel,
                    onDone = { navController.navigate(Screen.Dashboard.route) }
                )
            }

            composable(Screen.JoinRoom.route) {
                CreateJoinRoomScreen(
                    isCreateMode = false,
                    roomViewModel = roomViewModel,
                    authViewModel = authViewModel,
                    onDone = { navController.navigate(Screen.Dashboard.route) }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    navController = navController,
                    roomViewModel = roomViewModel,
                    expenseViewModel = expenseViewModel,
                    authViewModel = authViewModel,
                    settlementViewModel = settlementViewModel
                )
            }

            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    navController = navController,
                    expenseViewModel = expenseViewModel,
                    roomViewModel = roomViewModel,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.AddExpense.route) {
                AddEditExpenseScreen(
                    navController = navController,
                    expenseViewModel = expenseViewModel,
                    roomViewModel = roomViewModel,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Settlements.route) {
                SettlementsScreen(
                    settlementViewModel = settlementViewModel,
                    expenseViewModel = expenseViewModel,
                    roomViewModel = roomViewModel,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(
                    expenseViewModel = expenseViewModel,
                    roomViewModel = roomViewModel
                )
            }

            composable(Screen.Members.route) {
                MembersScreen(
                    roomViewModel = roomViewModel,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    roomViewModel = roomViewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
