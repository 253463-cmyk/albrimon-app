package com.albrimon.app.viewmodel.project

import androidx.lifecycle.ViewModel
import com.albrimon.app.data.model.Project
import com.albrimon.app.data.model.ProjectStatus
import com.albrimon.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProjectListViewModel : ViewModel() {

    private val allProjects: List<Project> = ProjectRepository.projects

    private val _uiState = MutableStateFlow(ProjectListUiState(projects = allProjects))
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
