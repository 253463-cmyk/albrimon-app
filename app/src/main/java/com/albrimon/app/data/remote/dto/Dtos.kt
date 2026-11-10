package com.albrimon.app.data.remote.dto

// DTOs: reflejan exactamente los records del backend Spring Boot (paquetes user, client, project y scrap).

// ---------- Autenticación ----------

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val linkCode: String? = null
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: String? = null
)

data class AuthResponse(val token: String, val tokenType: String? = null, val user: UserDto)

// ---------- Clientes ----------

data class ClientDto(
    val id: Long,
    val name: String,
    val phone: String? = null,
    val age: Int? = null,
    val size: String? = null,
    val active: Boolean = true,
    val bustCm: Int? = null,
    val waistCm: Int? = null,
    val hipCm: Int? = null,
    val lengthCm: Int? = null,
    val linkCode: String? = null
)

data class ClientRequest(
    val name: String,
    val phone: String? = null,
    val age: Int? = null,
    val size: String? = null,
    val bustCm: Int? = null,
    val waistCm: Int? = null,
    val hipCm: Int? = null,
    val lengthCm: Int? = null,
    val active: Boolean? = true
)

// ---------- Proyectos y piezas de patronaje ----------

data class ProjectDto(
    val id: Long,
    val name: String,
    val clientId: Long,
    val clientName: String? = null,
    val status: String? = null,
    /** Fecha ISO "2026-12-12" (LocalDate en el backend). */
    val dueDate: String? = null,
    val notes: String? = null
)

data class ProjectRequest(
    val name: String,
    val clientId: Long,
    val status: String? = null,
    val dueDate: String? = null,
    val notes: String? = null
)

data class PieceDto(
    val id: Long,
    val name: String,
    val widthMm: Int,
    val heightMm: Int,
    val canRotate: Boolean
)

data class PieceRequest(
    val name: String,
    val widthMm: Int,
    val heightMm: Int,
    val canRotate: Boolean
)

// ---------- Retazos ----------

data class ScrapDto(
    val id: Long,
    val name: String,
    val fabricType: String,
    val colorHex: String,
    val widthMm: Int,
    val heightMm: Int,
    val hasGrain: Boolean,
    val photoPath: String? = null,
    val available: Boolean
)

data class ScrapRequest(
    val name: String,
    val fabricType: String,
    val colorHex: String,
    val widthMm: Int,
    val heightMm: Int,
    val hasGrain: Boolean,
    val photoPath: String? = null
)

data class UsageInput(
    val scrapId: Long,
    val usedAreaMm2: Long,
    val leftoverWmm: Int,
    val leftoverHmm: Int
)

data class ApplyPlanRequest(val projectId: Long, val usages: List<UsageInput>)

data class UsageDto(
    val id: Long,
    val projectId: Long,
    val scrapId: Long,
    val scrapName: String,
    val usedAreaMm2: Long,
    val leftoverWmm: Int,
    val leftoverHmm: Int,
    val createdAt: String? = null
)
