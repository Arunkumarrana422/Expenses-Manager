package com.example.expensemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.expensemanager.ui.screens.ForgotPasswordScreen
import com.example.expensemanager.ui.screens.LoginScreen
import com.example.expensemanager.ui.screens.SignUpScreen
import com.example.expensemanager.viewmodel.AuthViewModel

@Composable
fun AuthNavGraph(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { /* handled by parent swap */ },
                onNavigateToSignUp = { navController.navigate("signup") },
                onNavigateToForgotPassword = { navController.navigate("forgot") }
            )
        }
        composable("signup") {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = { /* handled by parent swap */ },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("forgot") {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }
    }
}
