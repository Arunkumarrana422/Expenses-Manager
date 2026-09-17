package com.example.expensemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Splash only for first ~1 second of app launch
    var splashDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1000)
        splashDone = true
    }

    // Track only stable (definitive) states — ignore Loading/Idle
    // This prevents splash from showing during login
    var stableState by remember { mutableStateOf<AuthUiState>(AuthUiState.Idle) }
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> stableState = authState
            is AuthUiState.Unauthenticated -> stableState = authState
            else -> { /* keep previous stable state */ }
        }
    }

    when {
        !splashDone -> SplashContent()
        stableState is AuthUiState.Authenticated -> NavGraph(authViewModel = authViewModel)
        stableState is AuthUiState.Unauthenticated -> AuthNavGraph(authViewModel = authViewModel)
        else -> SplashContent()
    }
}

@Composable
private fun SplashContent() {
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
