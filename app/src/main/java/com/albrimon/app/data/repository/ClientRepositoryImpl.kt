package com.albrimon.app.data.repository

import com.albrimon.app.data.remote.AlbrimonApi
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.ClientRequest
import com.albrimon.app.data.remote.safeApiCall
import com.albrimon.app.domain.model.Client
import javax.inject.Inject
import javax.inject.Singleton
import com.albrimon.app.domain.repository.ClientRepository

/** Clientes del diseñador, desde la API REST (GET/POST /api/clients). */
@Singleton
class ClientRepositoryImpl @Inject constructor(
    private val api: AlbrimonApi
) : ClientRepository {
    override suspend fun clients(): ApiResult<List<Client>> =
        safeApiCall { api.clients().map { it.toDomain() } }

    override suspend fun client(id: Long): ApiResult<Client> =
        safeApiCall { api.client(id).toDomain() }

    override suspend fun create(request: ClientRequest): ApiResult<Client> =
        safeApiCall { api.createClient(request).toDomain() }
}
