package com.albrimon.app.presentation.project

import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus

data class ProjectListUiState(
    val searchQuery: String = "",
    // null = "Todo"
    val selectedStatus: ProjectStatus? = null,
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList(),
    val error: String? = null
)
