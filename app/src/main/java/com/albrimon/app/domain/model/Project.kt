package com.albrimon.app.domain.model

/** Mismos valores que el enum ProjectStatus del backend. */
enum class ProjectStatus(val label: String) {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En proceso"),
    ENTREGADO("Entregado"),
    TERMINADO("Terminado"),
    CANCELADO("Cancelado");

    companion object {
        fun fromApi(value: String?): ProjectStatus =
            entries.firstOrNull { it.name == value } ?: PENDIENTE
    }
}

data class Project(
    val id: Long,
    val clientId: Long,
    val name: String,
    val clientName: String,
    val status: ProjectStatus,
    /** Fecha de entrega ya formateada para mostrar ("12 de diciembre de 2026"). */
    val date: String,
    /** Fecha de entrega tal como la manda la API ("2026-12-12"), para volver a enviarla. */
    val dueDate: String? = null,
    val notes: String? = null
)

/** Pieza de patronaje de un proyecto. Medidas en milímetros, como en el backend. */
data class PatternPiece(
    val id: Long,
    val name: String,
    val widthMm: Int,
    val heightMm: Int,
    val canRotate: Boolean
)
