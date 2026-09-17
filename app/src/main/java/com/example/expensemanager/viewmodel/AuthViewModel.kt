package com.example.expensemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.model.User
import com.example.expensemanager.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val user: FirebaseUser, val profile: User?) : AuthUiState()
    object Unauthenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            repository.getAuthState().collect { fbUser ->
                if (fbUser != null) {
                    val profile = repository.getUserProfile(fbUser.uid).getOrNull()
                    _uiState.value = AuthUiState.Authenticated(fbUser, profile)
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.login(email, pass).fold(
                onSuccess = { fbUser ->
                    val profile = repository.getUserProfile(fbUser.uid).getOrNull()
                    _uiState.value = AuthUiState.Authenticated(fbUser, profile)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Login failed")
                }
            )
        }
    }

    fun register(name: String, email: String, pass: String) {
        if (name.isBlank() || email.isBlank() || pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.register(name, email, pass).fold(
                onSuccess = { fbUser ->
                    val profile = repository.getUserProfile(fbUser.uid).getOrNull()
                    _uiState.value = AuthUiState.Authenticated(fbUser, profile)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Registration failed")
                }
            )
        }
    }

    fun resetPassword(email: String, onSent: () -> Unit) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Enter your registered email")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.sendPasswordReset(email).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Unauthenticated
                    onSent()
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Password reset failed")
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState.Unauthenticated
    }
}
