package com.albrimon.app.presentation.scrap

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.albrimon.app.domain.model.Scrap
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.repository.AuthRepository
import com.albrimon.app.data.repository.ScrapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScrapListUiState(
    val scraps: List<Scrap> = emptyList(),
    val onlyAvailable: Boolean = true,
    val isLoading: Boolean = true,
    val isDesigner: Boolean = true,
    /** Mensaje recuperable (error de red, sesión, etc.). Si hay caché se sigue mostrando la lista. */
    val error: String? = null,
    val isOffline: Boolean = false,
    val sessionExpired: Boolean = false
)

private data class LoadState(
    val loading: Boolean = true,
    val error: String? = null,
    val offline: Boolean = false,
    val expired: Boolean = false
)

@HiltViewModel
class ScrapListViewModel @Inject constructor(
    private val repository: ScrapRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val onlyAvailable = MutableStateFlow(true)
    private val load = MutableStateFlow(LoadState())

    val uiState: StateFlow<ScrapListUiState> = combine(
        repository.scraps, onlyAvailable, load, auth.session
    ) { list, only, loadState, session ->
        ScrapListUiState(
            scraps = if (only) list.filter { it.available } else list,
            onlyAvailable = only,
            isLoading = loadState.loading,
            isDesigner = session?.isDesigner ?: true,
            error = loadState.error,
            isOffline = loadState.offline,
            sessionExpired = loadState.expired
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScrapListUiState())

    init {
        refresh()
    }

    fun setOnlyAvailable(value: Boolean) {
        onlyAvailable.value = value
    }

    /** Descarga el inventario. Si falla, conserva la caché de Room y avisa. */
    fun refresh() {
        viewModelScope.launch {
            val session = auth.session.first()
            if (session != null && !session.isDesigner) {
                load.value = LoadState(loading = false)
                return@launch
            }
            load.update { it.copy(loading = true, error = null) }
            when (val result = repository.refresh()) {
                is ApiResult.Success -> load.value = LoadState(loading = false)
                is ApiResult.Failure -> load.value = LoadState(
                    loading = false,
                    error = result.message,
                    offline = result.offline,
                    expired = result.unauthorized
                )
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            auth.logout()
            onDone()
        }
    }
}
