package com.albrimon.app.data.repository

import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus
object ProjectRepository {
    val projects: List<Project> = listOf(
        Project(1, 1, "Vestido midi", "Ana López", ProjectStatus.PENDIENTE, "12 de Diciembre del 2026"),
        Project(2, 2, "Pantalón de vestir", "Valeria Torres", ProjectStatus.EN_PROCESO, "22 de Diciembre del 2026"),
        Project(3, 4, "Camisa casual", "Daniela Cruz", ProjectStatus.TERMINADO, "12 de septiembre del 2026"),
        Project(4, 1, "Falda escolar", "Ana López", ProjectStatus.CANCELADO, "25 de Noviembre del 2026"),
        Project(5, 3, "Cardigan", "Sofía Ramírez", ProjectStatus.ENTREGADO, "24 de Agosto del 2026")
    )

    fun getByClient(clientId: Int): List<Project> = projects.filter { it.clientId == clientId }
}
