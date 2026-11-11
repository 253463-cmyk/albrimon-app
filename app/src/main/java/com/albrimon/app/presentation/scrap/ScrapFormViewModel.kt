package com.albrimon.app.presentation.scrap

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.albrimon.app.data.local.NewPhoto
import com.albrimon.app.data.local.PhotoStorage
import com.albrimon.app.domain.model.Scrap
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.ScrapRequest
import com.albrimon.app.domain.repository.ScrapRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScrapFormUiState(
    val scrapId: Long = 0L,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val fabricType: String = "",
    val colorHex: String = "#C9A66B",
    val widthCm: String = "",
    val heightCm: String = "",
    val hasGrain: Boolean = false,
    val photoPath: String? = null,
    /** false cuando el retazo ya se usó en un plan: solo se consulta. */
    val available: Boolean = true,
    val nameError: String? = null,
    val fabricError: String? = null,
    val colorError: String? = null,
    val widthError: String? = null,
    val heightError: String? = null,
    /** Error de guardado/eliminación: el formulario se conserva para reintentar. */
    val error: String? = null,
    /** Aviso de foto o permiso de cámara. */
    val photoMessage: String? = null,
    val cameraPermissionBlocked: Boolean = false,
    val done: Boolean = false
) {
    val isEditing: Boolean get() = scrapId != 0L
    val readOnly: Boolean get() = isEditing && !available
}

@HiltViewModel
class ScrapFormViewModel @Inject constructor(
    private val repository: ScrapRepository,
    private val photos: PhotoStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scrapId: Long = savedStateHandle.get<Long>("scrapId") ?: 0L

    private val _uiState = MutableStateFlow(ScrapFormUiState(scrapId = scrapId, isLoading = scrapId != 0L))
    val uiState: StateFlow<ScrapFormUiState> = _uiState.asStateFlow()

    private var loaded: Scrap? = null
    private var originalPhoto: String? = null
    private var pendingCapture: NewPhoto? = null
    private val photosCreatedHere = mutableSetOf<String>()
    private var saved = false

    init {
        if (scrapId != 0L) loadExisting()
    }

    private fun loadExisting() {
        viewModelScope.launch {
            val scrap = repository.getCached(scrapId)
            if (scrap == null) {
                _uiState.update { it.copy(isLoading = false, error = "No se encontró el retazo. Regresa y actualiza el inventario.") }
                return@launch
            }
            loaded = scrap
            originalPhoto = scrap.photoPath
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = scrap.name,
                    fabricType = scrap.fabricType,
                    colorHex = scrap.colorHex,
                    widthCm = mmToCm(scrap.widthMm),
                    heightCm = mmToCm(scrap.heightMm),
                    hasGrain = scrap.hasGrain,
                    photoPath = scrap.photoPath,
                    available = scrap.available
                )
            }
        }
    }

    fun onName(v: String) = _uiState.update { it.copy(name = v.take(80), nameError = null, error = null) }
    fun onFabric(v: String) = _uiState.update { it.copy(fabricType = v.take(40), fabricError = null, error = null) }
    fun onColor(v: String) = _uiState.update { it.copy(colorHex = v.take(7), colorError = null, error = null) }
    fun onWidth(v: String) = _uiState.update { it.copy(widthCm = v.take(7), widthError = null, error = null) }
    fun onHeight(v: String) = _uiState.update { it.copy(heightCm = v.take(7), heightError = null, error = null) }
    fun onGrain(v: Boolean) = _uiState.update { it.copy(hasGrain = v) }

    // ----------------------------- Fotos (cámara / galería) -----------------------------

    /** Crea el archivo destino en almacenamiento privado; la cámara escribirá ahí vía FileProvider. */
    fun prepareCapture(): NewPhoto? =
        runCatching { photos.newPhoto() }
            .onSuccess { pendingCapture = it }
            .onFailure { _uiState.update { s -> s.copy(photoMessage = "No se pudo preparar el archivo de la foto.") } }
            .getOrNull()

    fun onCameraResult(success: Boolean) {
        val capture = pendingCapture
        pendingCapture = null
        if (capture == null) return
        if (!success) {
            photos.delete(capture.relativePath)
            return
        }
        if (photos.file(capture.relativePath) == null) {
            photos.delete(capture.relativePath)
            _uiState.update { it.copy(photoMessage = "La cámara no guardó la foto. Intenta de nuevo.") }
            return
        }
        setPhoto(capture.relativePath)
    }

    fun onGalleryPicked(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            runCatching { photos.importFromGallery(uri) }
                .onSuccess { setPhoto(it) }
                .onFailure { e -> _uiState.update { it.copy(photoMessage = e.message ?: "No se pudo importar la imagen.") } }
        }
    }

    fun removePhoto() = setPhoto(null)

    private fun setPhoto(path: String?) {
        val previous = _uiState.value.photoPath
        if (previous != null && previous in photosCreatedHere) {
            photos.delete(previous)
            photosCreatedHere.remove(previous)
        }
        if (path != null) photosCreatedHere += path
        _uiState.update { it.copy(photoPath = path, photoMessage = null, cameraPermissionBlocked = false) }
    }

    fun onCameraPermissionDenied(blocked: Boolean) = _uiState.update {
        it.copy(
            cameraPermissionBlocked = blocked,
            photoMessage = if (blocked) {
                "El permiso de cámara está bloqueado. Actívalo en Ajustes o elige una foto de la galería."
            } else {
                "Sin permiso de cámara no puedo tomar la foto. Puedes elegir una de la galería."
            }
        )
    }

    fun onPhotoMessage(message: String) = _uiState.update { it.copy(photoMessage = message) }

    // ------------------------------- Guardar / eliminar --------------------------------

    fun save() {
        val s = _uiState.value
        if (s.isSaving || s.readOnly) return

        val widthMm = parseCmToMm(s.widthCm)
        val heightMm = parseCmToMm(s.heightCm)
        val validated = s.copy(
            nameError = if (s.name.isBlank()) "Escribe un nombre para identificarlo" else null,
            fabricError = if (s.fabricType.isBlank()) "Elige o escribe el tipo de tela" else null,
            colorError = if (!HEX_REGEX.matches(s.colorHex)) "Usa el formato #RRGGBB" else null,
            widthError = if (widthMm == null) "Ingresa un ancho entre 1 y 500 cm" else null,
            heightError = if (heightMm == null) "Ingresa un alto entre 1 y 500 cm" else null
        )
        if (listOf(validated.nameError, validated.fabricError, validated.colorError, validated.widthError, validated.heightError).any { it != null }) {
            _uiState.value = validated
            return
        }

        _uiState.value = validated.copy(isSaving = true, error = null)
        val request = ScrapRequest(
            name = s.name.trim(),
            fabricType = s.fabricType.trim(),
            colorHex = s.colorHex.uppercase(),
            widthMm = widthMm!!,
            heightMm = heightMm!!,
            hasGrain = s.hasGrain,
            photoPath = s.photoPath
        )
        viewModelScope.launch {
            when (val result = repository.save(scrapId.takeIf { it != 0L }, request)) {
                is ApiResult.Success -> {
                    saved = true
                    // La foto anterior ya no se usa: se libera el archivo.
                    if (originalPhoto != null && originalPhoto != s.photoPath) photos.delete(originalPhoto)
                    _uiState.update { it.copy(isSaving = false, done = true) }
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }

    fun delete() {
        val scrap = loaded ?: return
        if (_uiState.value.isSaving || !scrap.available) return
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.delete(scrap)) {
                is ApiResult.Success -> {
                    saved = true
                    _uiState.update { it.copy(isSaving = false, done = true) }
                }
                is ApiResult.Failure -> _uiState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }

    override fun onCleared() {
        // Si se sale sin guardar, no dejar fotos huérfanas en el almacenamiento.
        if (!saved) photosCreatedHere.forEach { photos.delete(it) }
        pendingCapture?.let { photos.delete(it.relativePath) }
    }

    private fun parseCmToMm(text: String): Int? {
        val cm = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
        val mm = (cm * 10).roundToInt()
        return mm.takeIf { it in 10..5000 }
    }

    private companion object {
        val HEX_REGEX = Regex("^#[0-9A-Fa-f]{6}$")
    }
}
