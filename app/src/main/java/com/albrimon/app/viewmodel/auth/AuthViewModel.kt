package com.albrimon.app.viewmodel.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    /** Paso 1 del diagrama: valida credenciales. Aquí después se llamará al Repositorio / API REST. */
    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        val error = validateCredentials(state.email, state.password)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
        } else {
            onSuccess()
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        val error = when {
            state.name.isBlank() -> "Ingresa tu nombre completo"
            else -> validateCredentials(state.email, state.password)
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
        } else {
            onSuccess()
        }
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        email.isBlank() || !email.contains("@") -> "Ingresa un correo electrónico válido"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }
}
