package com.example.expensemanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Login")
    object SignUp : Screen("signup", "Sign Up")
    object ForgotPassword : Screen("forgot_password", "Forgot Password")

    object CreateRoom : Screen("create_room", "Create Room")
    object JoinRoom : Screen("join_room", "Join Room")

    // Bottom Navigation Bar Items
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.AccountBalanceWallet)
    object Settlements : Screen("settlements", "Settlements", Icons.Default.AccountBalanceWallet)
    object Reports : Screen("reports", "Reports", Icons.Default.InsertChart)
    object Members : Screen("members", "Members", Icons.Default.Group)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    // Form screens
    object AddExpense : Screen("add_expense", "Add Expense")
    object EditExpense : Screen("edit_expense/{expenseId}", "Edit Expense") {
        fun createRoute(expenseId: String) = "edit_expense/$expenseId"
    }

    companion object {
        val bottomNavItems = listOf(Dashboard, Expenses, Reports, Members, Profile)
    }
}
