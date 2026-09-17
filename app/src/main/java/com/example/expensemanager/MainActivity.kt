package com.example.expensemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensemanager.ui.navigation.AuthNavGraph
import com.example.expensemanager.ui.navigation.NavGraph
import com.example.expensemanager.ui.theme.ExpenseManagerTheme
import com.example.expensemanager.viewmodel.AuthUiState
import com.example.expensemanager.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    when (authState) {
        is AuthUiState.Authenticated -> {
            // Logged in — show main app
            NavGraph(authViewModel = authViewModel)
        }
        is AuthUiState.Unauthenticated, is AuthUiState.Error -> {
            // Logged out — show login/signup
            AuthNavGraph(authViewModel = authViewModel)
        }
        else -> {
            // Idle / Loading — show splash
            SplashContent()
        }
    }
}

@Composable
private fun SplashContent() {
    // Small delay so user sees the splash briefly
    LaunchedEffect(Unit) { delay(500) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.25f),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
