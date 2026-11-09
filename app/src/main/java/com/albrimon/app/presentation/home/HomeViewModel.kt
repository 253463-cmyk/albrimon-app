package com.albrimon.app.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DesignServices
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albrimon.app.core.ui.theme.AzulAgua
import com.albrimon.app.core.ui.theme.NaranjaSuave
import com.albrimon.app.core.ui.theme.RosaEmpolvado
import com.albrimon.app.core.ui.theme.RosaSuave
import com.albrimon.app.core.ui.theme.VerdeSalvia
import com.albrimon.app.data.repository.AuthRepository
import com.albrimon.app.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val modules = listOf(
        HomeModule("Clientes", "Gestiona sus medidas\ny proyectos", Icons.Outlined.PersonOutline, AzulAgua, Routes.CLIENTS),
        HomeModule("Proyectos", "Crea y consulta\ntus prendas", Icons.Outlined.FolderOpen, RosaSuave, Routes.PROJECTS),
        HomeModule("Patrones", "Calcula y diseña", Icons.Outlined.DesignServices, NaranjaSuave, Routes.soon("Patrones")),
        HomeModule("Materiales", "Telas y retazos", Icons.Outlined.Layers, VerdeSalvia, Routes.SCRAPS),
        HomeModule("Costos", "Presupuesto y venta", Icons.Outlined.Paid, RosaSuave, Routes.COSTS),
        HomeModule("Nuevo proyecto", "Comienza a diseñar", Icons.Filled.Add, RosaEmpolvado, Routes.soon("Nuevo proyecto"))
    )

    /** El nombre sale de la sesión guardada (DataStore), ya no es un valor fijo. */
    val uiState: StateFlow<HomeUiState> = authRepository.session
        .map { session ->
            HomeUiState(
                userName = session?.name?.trim()?.substringBefore(' ').orEmpty(),
                modules = modules
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(modules = modules)
        )

    /** Borra el token y la copia local, y avisa a la pantalla para regresar a Bienvenida. */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
