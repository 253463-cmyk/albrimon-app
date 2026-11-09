package com.albrimon.app.domain.model

/** Sesión del usuario que inició sesión. El token es el JWT que entrega el backend. */
data class Session(
    val token: String,
    val userId: String,
    val name: String,
    val email: String,
    val role: String
) {
    val isDesigner: Boolean get() = role == ROLE_DESIGNER

    companion object {
        const val ROLE_DESIGNER = "DISENADOR"
        const val ROLE_CLIENT = "CLIENTE"
    }
}
