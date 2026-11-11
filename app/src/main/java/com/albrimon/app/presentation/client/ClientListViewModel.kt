package com.albrimon.app.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.domain.repository.ClientRepository
import com.albrimon.app.domain.model.Client
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ClientListViewModel @Inject constructor(
    private val repository: ClientRepository
) : ViewModel() {

    private var allClients: List<Client> = emptyList()

    private val _uiState = MutableStateFlow(ClientListUiState(isLoading = true))
    val uiState: StateFlow<ClientListUiState> = _uiState.asStateFlow()

    /** Se llama al abrir la pantalla y al regresar del formulario, para ver al cliente nuevo. */
    fun refresh() {
        _uiState.update { it.copy(isLoading = allClients.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = repository.clients()) {
                is ApiResult.Success -> {
                    allClients = result.data
                    _uiState.update { applyFilters(it.copy(isLoading = false)) }
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { applyFilters(it.copy(searchQuery = query)) }
    }

    fun onFilterChange(filter: ClientFilter) {
        _uiState.update { applyFilters(it.copy(filter = filter)) }
    }

    // REQ-4: buscar y filtrar clientes por nombre o por estado (activo/inactivo)
    private fun applyFilters(state: ClientListUiState): ClientListUiState {
        val filtered = allClients.filter { client ->
            val matchesQuery = state.searchQuery.isBlank() ||
                client.name.contains(state.searchQuery, ignoreCase = true)
            val matchesFilter = when (state.filter) {
                ClientFilter.TODOS -> true
                ClientFilter.ACTIVOS -> client.isActive
                ClientFilter.INACTIVOS -> !client.isActive
            }
            matchesQuery && matchesFilter
        }
        return state.copy(clients = filtered)
    }
}
