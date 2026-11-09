package com.albrimon.app.data.repository

import com.albrimon.app.data.local.AlbrimonDatabase
import com.albrimon.app.data.local.TokenStore
import com.albrimon.app.data.remote.AlbrimonApi
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.AuthResponse
import com.albrimon.app.data.remote.dto.LoginRequest
import com.albrimon.app.data.remote.dto.RegisterRequest
import com.albrimon.app.data.remote.safeApiCall
import com.albrimon.app.domain.model.Session
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Autenticación contra la API (POST /api/auth/login y /api/auth/register).
 * Guarda el JWT y los datos del usuario en DataStore para mantener la sesión.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: AlbrimonApi,
    private val tokenStore: TokenStore,
    private val database: AlbrimonDatabase
) {
    val session: Flow<Session?> = tokenStore.session

    suspend fun hasSession(): Boolean = tokenStore.hasSession()

    suspend fun login(email: String, password: String): ApiResult<Session> =
        persist(safeApiCall { api.login(LoginRequest(email.trim(), password)) })

    suspend fun register(name: String, email: String, password: String): ApiResult<Session> =
        persist(safeApiCall { api.register(RegisterRequest(name.trim(), email.trim(), password)) })

    /** Cierra sesión y borra la copia local para no mostrar datos de otra cuenta. */
    suspend fun logout() {
        tokenStore.clear()
        withContext(Dispatchers.IO) { database.clearAllTables() }
    }

    private suspend fun persist(result: ApiResult<AuthResponse>): ApiResult<Session> =
        when (result) {
            is ApiResult.Success -> {
                val user = result.data.user
                val session = Session(result.data.token, user.id, user.name, user.email, user.role)
                tokenStore.save(session)
                ApiResult.Success(session)
            }
            is ApiResult.Failure -> result
        }
}
