package com.albrimon.app.domain.repository

import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.ClientRequest
import com.albrimon.app.domain.model.Client

/** Contrato de clientes del diseñador. */
interface ClientRepository {
    suspend fun clients(): ApiResult<List<Client>>

    suspend fun client(id: Long): ApiResult<Client>

    suspend fun create(request: ClientRequest): ApiResult<Client>
}
