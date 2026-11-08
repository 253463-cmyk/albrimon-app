package com.albrimon.app.viewmodel.client

import androidx.lifecycle.ViewModel
import com.albrimon.app.data.model.Client
import com.albrimon.app.data.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClientListViewModel : ViewModel() {

    private val allClients: List<Client> = ClientRepository.clients

    private val _uiState = MutableStateFlow(ClientListUiState(clients = allClients))
    val uiState: StateFlow<ClientListUiState> = _uiState.asStateFlow()

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
