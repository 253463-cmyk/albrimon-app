package com.albrimon.app.presentation.client

import com.albrimon.app.domain.model.Client

enum class ClientFilter(val label: String) {
    TODOS("Todos"),
    ACTIVOS("Activos"),
    INACTIVOS("Inactivos")
}

data class ClientListUiState(
    val searchQuery: String = "",
    val filter: ClientFilter = ClientFilter.TODOS,
    val isLoading: Boolean = false,
    val clients: List<Client> = emptyList(),
    val error: String? = null
)
