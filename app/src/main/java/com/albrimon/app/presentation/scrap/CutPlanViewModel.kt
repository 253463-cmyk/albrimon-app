package com.albrimon.app.presentation.scrap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.albrimon.app.domain.model.Scrap
import com.albrimon.app.domain.model.ScrapUsage
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.PieceDto
import com.albrimon.app.data.remote.dto.ProjectDto
import com.albrimon.app.data.remote.dto.UsageInput
import com.albrimon.app.data.repository.ScrapRepository
import com.albrimon.app.domain.cut.CutPlan
import com.albrimon.app.domain.cut.CutPlanner
import com.albrimon.app.domain.cut.Piece
import com.albrimon.app.domain.cut.ScrapSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CutPlanUiState(
    val projects: List<ProjectDto> = emptyList(),
    val loadingProjects: Boolean = true,
    val selected: ProjectDto? = null,
    val pieces: List<PieceDto> = emptyList(),
    val loadingPieces: Boolean = false,
    /** Solo retazos disponibles (los usados ya no entran al cálculo). */
    val scraps: List<Scrap> = emptyList(),
    val plan: CutPlan? = null,
    val calculating: Boolean = false,
    val applying: Boolean = false,
    val history: List<ScrapUsage> = emptyList(),
    val error: String? = null,
    val success: String? = null
)

@HiltViewModel
class CutPlanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ScrapRepository
) : ViewModel() {

    /** Si se abre desde el detalle de un proyecto, llega su id y se selecciona solo. */
    private val preselectedProjectId: Long? =
        savedStateHandle.get<Long>("projectId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(CutPlanUiState())
    val uiState: StateFlow<CutPlanUiState> = _uiState.asStateFlow()

    private var historyJob: Job? = null

    init {
        viewModelScope.launch {
            repository.scraps.collect { list ->
                _uiState.update { it.copy(scraps = list.filter { s -> s.available }) }
            }
        }
        loadProjects()
    }

    fun loadProjects() {
        _uiState.update { it.copy(loadingProjects = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.projects()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(loadingProjects = false, projects = result.data) }
                    val preselected = result.data.firstOrNull { it.id == preselectedProjectId }
                    if (preselected != null && _uiState.value.selected == null) selectProject(preselected)
                }
                is ApiResult.Failure -> _uiState.update { it.copy(loadingProjects = false, error = result.message) }
            }
        }
    }

    fun selectProject(project: ProjectDto) {
        historyJob?.cancel()
        _uiState.update {
            it.copy(selected = project, pieces = emptyList(), plan = null, history = emptyList(), error = null, success = null)
        }
        loadPieces(project)
        historyJob = viewModelScope.launch {
            launch { repository.refreshUsage(project.id) }
            repository.usage(project.id).collect { list -> _uiState.update { it.copy(history = list) } }
        }
    }

    fun retryPieces() {
        _uiState.value.selected?.let { loadPieces(it) }
    }

    private fun loadPieces(project: ProjectDto) {
        _uiState.update { it.copy(loadingPieces = true) }
        viewModelScope.launch {
            when (val result = repository.pieces(project.id)) {
                is ApiResult.Success -> _uiState.update { it.copy(loadingPieces = false, pieces = result.data) }
                is ApiResult.Failure -> _uiState.update { it.copy(loadingPieces = false, error = result.message) }
            }
        }
    }

    /** Ejecuta CutPlanner localmente (Kotlin). No toca la red: solo calcula. */
    fun calculate() {
        val s = _uiState.value
        val error = when {
            s.selected == null -> "Elige primero un proyecto."
            s.pieces.isEmpty() -> "Este proyecto no tiene piezas de patronaje registradas."
            s.scraps.isEmpty() -> "No hay retazos disponibles. Registra uno en el inventario."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error, success = null) }
            return
        }
        _uiState.update { it.copy(calculating = true, error = null, success = null) }
        viewModelScope.launch {
            val plan = withContext(Dispatchers.Default) {
                CutPlanner.plan(
                    scraps = s.scraps.map { ScrapSlot(it.id, it.name, it.widthMm, it.heightMm, it.hasGrain) },
                    pieces = s.pieces.map { Piece(it.id, it.name, it.widthMm, it.heightMm, it.canRotate) }
                )
            }
            _uiState.update { it.copy(calculating = false, plan = plan) }
        }
    }

    /** Envía el plan a POST /api/scraps/apply-plan. Solo se confirma si la API respondió OK. */
    fun apply() {
        val s = _uiState.value
        val project = s.selected ?: return
        val plan = s.plan ?: return
        if (s.applying) return
        if (!plan.isComplete) {
            _uiState.update { it.copy(error = "Hay piezas sin acomodar. Agrega más retazos antes de aplicar el plan.") }
            return
        }
        _uiState.update { it.copy(applying = true, error = null, success = null) }
        val usages = plan.uses.map { UsageInput(it.scrapId, it.usedAreaMm2, it.leftoverWmm, it.leftoverHmm) }
        viewModelScope.launch {
            when (val result = repository.applyPlan(project.id, usages)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        applying = false,
                        plan = null,
                        success = "Plan aplicado: ${result.data} retazo(s) consumido(s). Los sobrantes útiles ya están en el inventario."
                    )
                }
                // El plan se conserva para reintentar; no se afirma que se guardó.
                is ApiResult.Failure -> _uiState.update { it.copy(applying = false, error = result.message) }
            }
        }
    }

    fun dismissMessages() = _uiState.update { it.copy(error = null, success = null) }
}
