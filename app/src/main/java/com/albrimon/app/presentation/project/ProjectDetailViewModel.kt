package com.albrimon.app.presentation.project

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.file.TechSheetPdfGenerator
import com.albrimon.app.data.remote.dto.PieceRequest
import com.albrimon.app.data.repository.ClientRepository
import com.albrimon.app.data.repository.ProjectRepository
import com.albrimon.app.domain.model.PatternPiece
import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val pieces: List<PatternPiece> = emptyList(),
    val isBusy: Boolean = false,
    val error: String? = null,
    /** Mensaje corto tras una acción ("Estado actualizado"). */
    val message: String? = null,
    /** PDF recién generado: la pantalla lo comparte y luego llama a onPdfShared(). */
    val pdfFile: File? = null
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProjectRepository,
    private val clientRepository: ClientRepository,
    private val pdfGenerator: TechSheetPdfGenerator
) : ViewModel() {

    private val projectId: Long = savedStateHandle.get<Long>("projectId") ?: 0L

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = it.project == null, error = null) }
        viewModelScope.launch {
            when (val result = repository.project(projectId)) {
                is ApiResult.Success -> {
                    val pieces = repository.pieces(projectId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            project = result.data,
                            pieces = (pieces as? ApiResult.Success)?.data ?: it.pieces,
                            error = (pieces as? ApiResult.Failure)?.message
                        )
                    }
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    /** PUT /api/projects/{id} con el estado nuevo. */
    fun changeStatus(status: ProjectStatus) {
        val project = _uiState.value.project ?: return
        if (status == project.status || _uiState.value.isBusy) return
        _uiState.update { it.copy(isBusy = true, error = null, message = null) }
        viewModelScope.launch {
            when (val result = repository.updateStatus(project, status)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isBusy = false, project = result.data, message = "Estado actualizado a ${status.label}")
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isBusy = false, error = result.message) }
            }
        }
    }

    /**
     * Agrega una pieza de patronaje. Las medidas se capturan en cm y el backend las guarda en mm.
     * Devuelve un mensaje de error de validación, o null si la petición se envió.
     */
    fun addPiece(name: String, widthCm: String, heightCm: String, canRotate: Boolean): String? {
        val width = widthCm.trim().replace(',', '.').toDoubleOrNull()
        val height = heightCm.trim().replace(',', '.').toDoubleOrNull()
        val error = when {
            name.isBlank() -> "Escribe el nombre de la pieza"
            width == null || width <= 0 -> "El ancho debe ser mayor a 0"
            height == null || height <= 0 -> "El alto debe ser mayor a 0"
            else -> null
        }
        if (error != null) return error

        val request = PieceRequest(
            name = name.trim(),
            widthMm = (width!! * 10).roundToInt(),
            heightMm = (height!! * 10).roundToInt(),
            canRotate = canRotate
        )
        _uiState.update { it.copy(isBusy = true, error = null, message = null) }
        viewModelScope.launch {
            when (val result = repository.addPiece(projectId, request)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isBusy = false, pieces = it.pieces + result.data, message = "Pieza agregada")
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isBusy = false, error = result.message) }
            }
        }
        return null
    }

    fun deletePiece(piece: PatternPiece) {
        if (_uiState.value.isBusy) return
        _uiState.update { it.copy(isBusy = true, error = null, message = null) }
        viewModelScope.launch {
            when (val result = repository.deletePiece(projectId, piece.id)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isBusy = false, pieces = it.pieces.filterNot { p -> p.id == piece.id }, message = "Pieza eliminada")
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isBusy = false, error = result.message) }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    /**
     * Genera la ficha técnica en PDF (proyecto + medidas del cliente + piezas).
     * Si no se pueden consultar las medidas, el PDF se genera igual sin esa sección.
     */
    fun generatePdf() {
        val project = _uiState.value.project ?: return
        if (_uiState.value.isBusy) return
        _uiState.update { it.copy(isBusy = true, error = null, message = null) }
        viewModelScope.launch {
            val client = (clientRepository.client(project.clientId) as? ApiResult.Success)?.data
            try {
                val file = pdfGenerator.generate(project, client, _uiState.value.pieces)
                _uiState.update { it.copy(isBusy = false, pdfFile = file) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, error = "No se pudo generar el PDF: ${e.message ?: "error desconocido"}") }
            }
        }
    }

    fun onPdfShared() = _uiState.update { it.copy(pdfFile = null, message = "Ficha técnica generada") }
}
