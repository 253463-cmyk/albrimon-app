package com.albrimon.app.presentation.auth

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    /** Código de vínculo opcional (se escribe o se escanea del QR que muestra el diseñador). */
    val linkCode: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
