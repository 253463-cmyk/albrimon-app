package com.albrimon.app.presentation.client

import com.albrimon.app.domain.model.Client

data class ClientDetailUiState(
    val client: Client? = null,
    val relatedProjects: Int = 0,
    val measurementsExpanded: Boolean = false
)
