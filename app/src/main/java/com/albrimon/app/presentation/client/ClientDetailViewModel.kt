package com.albrimon.app.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.domain.repository.ClientRepository
import com.albrimon.app.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState: StateFlow<ClientDetailUiState> = _uiState.asStateFlow()

    fun load(clientId: Long) {
        _uiState.update { it.copy(isLoading = it.client == null, error = null) }
        viewModelScope.launch {
            when (val result = clientRepository.client(clientId)) {
                is ApiResult.Success -> {
                    // Si falla el conteo de proyectos no se bloquea el detalle: se muestra 0
                    val related = (projectRepository.projectsByClient(clientId) as? ApiResult.Success)
                        ?.data?.size ?: 0
                    _uiState.update {
                        it.copy(isLoading = false, client = result.data, relatedProjects = related)
                    }
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun toggleMeasurements() {
        _uiState.update { it.copy(measurementsExpanded = !it.measurementsExpanded) }
    }
}
