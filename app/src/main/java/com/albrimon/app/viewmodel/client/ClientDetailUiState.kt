package com.albrimon.app.viewmodel.client

import com.albrimon.app.data.model.Client

data class ClientDetailUiState(
    val client: Client? = null,
    val relatedProjects: Int = 0,
    val measurementsExpanded: Boolean = false
)
