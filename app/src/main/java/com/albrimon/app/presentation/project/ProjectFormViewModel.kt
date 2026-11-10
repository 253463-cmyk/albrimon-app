package com.albrimon.app.presentation.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.repository.ClientRepository
import com.albrimon.app.data.repository.ProjectRepository
import com.albrimon.app.domain.model.Client
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectFormUiState(
    val clients: List<Client> = emptyList(),
    val loadingClients: Boolean = true,
    val name: String = "",
    val selectedClient: Client? = null,
    /** Fecha ISO "2026-12-12" que se envía a la API; null si no se eligió. */
    val dueDate: String? = null,
    val notes: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProjectFormViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectFormUiState())
    val uiState: StateFlow<ProjectFormUiState> = _uiState.asStateFlow()

    init {
        loadClients()
    }

    /** El proyecto se asigna a un cliente, así que primero se carga la lista (solo activos). */
    fun loadClients() {
        _uiState.update { it.copy(loadingClients = true, error = null) }
        viewModelScope.launch {
            when (val result = clientRepository.clients()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(loadingClients = false, clients = result.data.filter { c -> c.isActive })
                }
                is ApiResult.Failure -> _uiState.update { it.copy(loadingClients = false, error = result.message) }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onClientSelected(client: Client) = _uiState.update { it.copy(selectedClient = client, error = null) }
    fun onDueDateSelected(isoDate: String?) = _uiState.update { it.copy(dueDate = isoDate, error = null) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value, error = null) }

    /** POST /api/projects. Al terminar entrega el id para abrir el detalle y agregar piezas. */
    fun save(onSaved: (projectId: Long) -> Unit) {
        val s = _uiState.value
        if (s.isSaving) return
        val client = s.selectedClient
        val error = when {
            s.name.isBlank() -> "Escribe el nombre del proyecto"
            client == null -> "Selecciona el cliente"
            else -> null
        }
        if (error != null || client == null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val result = projectRepository.create(
                name = s.name.trim(),
                clientId = client.id,
                dueDate = s.dueDate,
                notes = s.notes.trim().ifBlank { null }
            )
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onSaved(result.data.id)
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }
}
