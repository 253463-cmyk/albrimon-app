package com.albrimon.app.presentation.cost

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Estado de la calculadora de costos. Sobrevive a la rotación de pantalla (vive en el ViewModel). */
@HiltViewModel
class CostViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CostUiState())
    val uiState: StateFlow<CostUiState> = _uiState.asStateFlow()

    private var nextId = 2

    fun addMaterial() = _uiState.update {
        it.copy(materials = it.materials + MaterialInput(id = nextId++))
    }

    /** Siempre queda al menos un material en la lista. */
    fun removeMaterial(id: Int) = _uiState.update { state ->
        if (state.materials.size <= 1) state
        else state.copy(materials = state.materials.filterNot { it.id == id })
    }

    fun updateMaterial(id: Int, transform: (MaterialInput) -> MaterialInput) = _uiState.update { state ->
        state.copy(materials = state.materials.map { if (it.id == id) transform(it) else it })
    }

    fun onLaborChange(value: String) = _uiState.update { it.copy(labor = value) }
    fun onContingencyChange(value: String) = _uiState.update { it.copy(contingencyPct = value) }
    fun onTaxChange(value: String) = _uiState.update { it.copy(taxPct = value) }

    fun reset() {
        nextId = 2
        _uiState.value = CostUiState()
    }
}
