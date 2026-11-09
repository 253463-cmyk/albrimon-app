package com.albrimon.app.data.remote

import com.google.gson.Gson
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

/** Resultado de una llamada a la API: nunca lanza excepciones hacia el ViewModel. */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    /**
     * @param offline true si no se pudo contactar al servidor (se puede mostrar la copia local).
     * @param unauthorized true si el token expiró o no es válido.
     */
    data class Failure(
        val message: String,
        val offline: Boolean = false,
        val unauthorized: Boolean = false
    ) : ApiResult<Nothing>
}

private data class ErrorBody(val error: String? = null)

/** Ejecuta una llamada de Retrofit y traduce los errores a mensajes en español. */
suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        val serverMessage = runCatching {
            Gson().fromJson(e.response()?.errorBody()?.string(), ErrorBody::class.java)?.error
        }.getOrNull()
        when (e.code()) {
            401 -> ApiResult.Failure(
                serverMessage ?: "Tu sesión expiró. Inicia sesión de nuevo.",
                unauthorized = true
            )
            403 -> ApiResult.Failure(
                serverMessage ?: "No tienes permiso para esta acción o tu sesión expiró.",
                unauthorized = true
            )
            in 500..599 -> ApiResult.Failure(serverMessage ?: "El servidor tuvo un problema. Intenta de nuevo.")
            else -> ApiResult.Failure(serverMessage ?: "No se pudo completar la operación (${e.code()}).")
        }
    } catch (e: IOException) {
        ApiResult.Failure(
            "Sin conexión con el servidor. Revisa que el backend esté encendido y la URL sea correcta.",
            offline = true
        )
    } catch (e: Exception) {
        ApiResult.Failure("Ocurrió un error inesperado: ${e.message ?: "intenta de nuevo"}")
    }
