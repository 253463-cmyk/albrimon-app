package com.albrimon.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onLinkCodeChange(value: String) = _uiState.update { it.copy(linkCode = value.trim().uppercase(), error = null) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    /** Valida los campos y, si son correctos, inicia sesión contra POST /api/auth/login. */
    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isLoading) return
        val error = validateCredentials(state.email, state.password)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        launchAuth(onSuccess) { repository.login(state.email, state.password) }
    }

    /** Valida los campos y crea la cuenta con POST /api/auth/register. */
    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isLoading) return
        val error = when {
            state.name.isBlank() -> "Ingresa tu nombre completo"
            else -> validateCredentials(state.email, state.password)
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        launchAuth(onSuccess) { repository.register(state.name, state.email, state.password, state.linkCode) }
    }

    private fun launchAuth(onSuccess: () -> Unit, call: suspend () -> ApiResult<*>) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = call()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, password = "") }
                    onSuccess()
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        email.isBlank() || !email.contains("@") -> "Ingresa un correo electrónico válido"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }
}
