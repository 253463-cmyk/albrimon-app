package com.albrimon.app.domain.model

enum class ProjectStatus(val label: String) {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En proceso"),
    ENTREGADO("Entregado"),
    TERMINADO("Terminado"),
    CANCELADO("Cancelado")
}

data class Project(
    val id: Int,
    val name: String,
    val clientName: String,
    val status: ProjectStatus,
    val date: String
)
