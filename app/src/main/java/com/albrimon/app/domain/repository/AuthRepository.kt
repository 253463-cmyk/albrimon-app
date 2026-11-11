package com.albrimon.app.domain.repository

import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.domain.model.Session
import kotlinx.coroutines.flow.Flow

/** Contrato de autenticación: la UI y los ViewModels dependen de esto, no de Retrofit ni DataStore. */
interface AuthRepository {
    val session: Flow<Session?>

    suspend fun hasSession(): Boolean

    suspend fun login(email: String, password: String): ApiResult<Session>

    /** linkCode (opcional) vincula la cuenta nueva con su ficha de cliente del taller. */
    suspend fun register(name: String, email: String, password: String, linkCode: String = ""): ApiResult<Session>

    /** Cierra sesión y borra la copia local para no mostrar datos de otra cuenta. */
    suspend fun logout()
}
