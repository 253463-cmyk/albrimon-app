package com.albrimon.app.presentation.project

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.domain.repository.ProjectRepository
import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProjectRepository
) : ViewModel() {

    // clientId llega como argumento de navegación; si es null se muestran todos los proyectos
    private val clientId: Long? = savedStateHandle.get<Long>("clientId")

    private var allProjects: List<Project> = emptyList()

    private val _uiState = MutableStateFlow(ProjectListUiState(isLoading = true))
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    /** Se llama al abrir la pantalla y al regresar de un detalle o formulario. */
    fun refresh() {
        _uiState.update { it.copy(isLoading = allProjects.isEmpty(), error = null) }
        viewModelScope.launch {
            val result = if (clientId == null) repository.projects() else repository.projectsByClient(clientId)
            when (result) {
                is ApiResult.Success -> {
                    allProjects = result.data
                    _uiState.update {
                        applyFilters(
                            it.copy(
                                isLoading = false,
                                // El nombre del cliente viene en cada proyecto
                                clientName = if (clientId != null) result.data.firstOrNull()?.clientName else null
                            )
                        )
                    }
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

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
