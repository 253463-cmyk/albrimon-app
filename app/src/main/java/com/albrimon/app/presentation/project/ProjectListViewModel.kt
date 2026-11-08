package com.albrimon.app.presentation.project

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus
import com.albrimon.app.data.repository.ClientRepository
import com.albrimon.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProjectListViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    // clientId llega como argumento de navegación; si es null se muestran todos los proyectos
    private val clientId: Int? = savedStateHandle.get<Int>("clientId")

    private val allProjects: List<Project> =
        if (clientId == null) ProjectRepository.projects else ProjectRepository.getByClient(clientId)

    private val clientName: String? = clientId?.let { ClientRepository.getById(it)?.name }

    private val _uiState = MutableStateFlow(
        ProjectListUiState(projects = allProjects, clientName = clientName)
    )
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { applyFilters(it.copy(searchQuery = query)) }
    }

    fun onStatusSelected(status: ProjectStatus?) {
        _uiState.update { applyFilters(it.copy(selectedStatus = status)) }
    }

    private fun applyFilters(state: ProjectListUiState): ProjectListUiState {
        val filtered = allProjects.filter { project ->
            val matchesQuery = state.searchQuery.isBlank() ||
                project.name.contains(state.searchQuery, ignoreCase = true) ||
                project.clientName.contains(state.searchQuery, ignoreCase = true)
            val matchesStatus = state.selectedStatus == null || project.status == state.selectedStatus
            matchesQuery && matchesStatus
        }
        return state.copy(projects = filtered)
    }
}
