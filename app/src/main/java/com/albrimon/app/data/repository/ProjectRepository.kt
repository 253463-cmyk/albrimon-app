package com.albrimon.app.data.repository

import com.albrimon.app.data.model.Project
import com.albrimon.app.data.model.ProjectStatus
object ProjectRepository {
    val projects: List<Project> = listOf(
        Project(1, "Vestido midi", "Ana López", ProjectStatus.PENDIENTE, "12 de Diciembre del 2026"),
        Project(2, "Pantalón de vestir", "Sergio Pérez", ProjectStatus.EN_PROCESO, "22 de Diciembre del 2026"),
        Project(3, "Camisa casual", "Yael Gómez", ProjectStatus.TERMINADO, "12 de septiembre del 2026"),
        Project(4, "Falda escolar", "Ana López", ProjectStatus.CANCELADO, "25 de Noviembre del 2026"),
        Project(5, "Cardigan", "Sofía Ramírez", ProjectStatus.ENTREGADO, "24 de Agosto del 2026")
    )
}
