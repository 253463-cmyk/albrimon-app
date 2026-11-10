package com.albrimon.app.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.ClientRequest
import com.albrimon.app.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientFormUiState(
    val name: String = "",
    val phone: String = "",
    val age: String = "",
    val size: String = "",
    val bust: String = "",
    val waist: String = "",
    val hip: String = "",
    val length: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ClientFormViewModel @Inject constructor(
    private val repository: ClientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientFormUiState())
    val uiState: StateFlow<ClientFormUiState> = _uiState.asStateFlow()

    fun update(transform: (ClientFormUiState) -> ClientFormUiState) =
        _uiState.update { transform(it).copy(error = null) }

    /** Valida y registra el cliente con POST /api/clients. */
    fun save(onSaved: () -> Unit) {
        val s = _uiState.value
        if (s.isSaving) return

        val age = s.age.trim().toIntOrNull()
        val measures = listOf(s.bust, s.waist, s.hip, s.length).map { it.trim() }
        val error = when {
            s.name.isBlank() -> "Escribe el nombre del cliente"
            s.age.isNotBlank() && (age == null || age !in 1..120) -> "La edad debe ser un número entre 1 y 120"
            measures.any { it.isNotEmpty() && (it.toIntOrNull() == null || it.toInt() < 0) } ->
                "Las medidas deben ser números enteros en centímetros"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        val request = ClientRequest(
            name = s.name.trim(),
            phone = s.phone.trim().ifBlank { null },
            age = age,
            size = s.size.trim().uppercase().ifBlank { null },
            bustCm = s.bust.trim().toIntOrNull(),
            waistCm = s.waist.trim().toIntOrNull(),
            hipCm = s.hip.trim().toIntOrNull(),
            lengthCm = s.length.trim().toIntOrNull(),
            active = true
        )
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.create(request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onSaved()
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }
}
