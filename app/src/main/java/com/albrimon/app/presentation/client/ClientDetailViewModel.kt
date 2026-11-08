package com.albrimon.app.presentation.client

import androidx.lifecycle.ViewModel
import com.albrimon.app.data.repository.ClientRepository
import com.albrimon.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClientDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState: StateFlow<ClientDetailUiState> = _uiState.asStateFlow()

    fun load(clientId: Int) {
        val client = ClientRepository.getById(clientId)
        val related = ProjectRepository.projects.count { it.clientName == client?.name }
        _uiState.update { it.copy(client = client, relatedProjects = related) }
    }

    fun toggleMeasurements() {
        _uiState.update { it.copy(measurementsExpanded = !it.measurementsExpanded) }
    }
}
