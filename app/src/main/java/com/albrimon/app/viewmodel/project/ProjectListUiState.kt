package com.albrimon.app.viewmodel.project

import com.albrimon.app.data.model.Project
import com.albrimon.app.data.model.ProjectStatus

data class ProjectListUiState(
    val searchQuery: String = "",
    // null = "Todo"
    val selectedStatus: ProjectStatus? = null,
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList(),
    val error: String? = null
)
