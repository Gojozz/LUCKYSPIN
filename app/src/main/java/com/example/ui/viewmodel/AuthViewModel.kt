package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepository.currentUser

    private val _authState = MutableStateFlow<String?>(null) // null, error message, or success message
    val authState: StateFlow<String?> = _authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepository.login(email, pass)
            _isLoading.value = false
            if (res.isSuccess) {
                _authState.value = null
                onSuccess()
            } else {
                _authState.value = res.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun register(email: String, pass: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepository.register(email, pass, name)
            _isLoading.value = false
            if (res.isSuccess) {
                _authState.value = null
                onSuccess()
            } else {
                _authState.value = res.exceptionOrNull()?.message ?: "Registration failed"
            }
        }
    }

    fun resetPassword(email: String, newPass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepository.resetPassword(email, newPass)
            _isLoading.value = false
            if (res.isSuccess) {
                _authState.value = "Password reset successfully! Please login with your new password."
            } else {
                _authState.value = res.exceptionOrNull()?.message ?: "Reset failed"
            }
        }
    }

    fun clearError() {
        _authState.value = null
    }

    fun logout() {
        authRepository.logout()
    }

    fun refreshUser() {
        viewModelScope.launch {
            authRepository.refreshCurrentUser()
        }
    }
}
