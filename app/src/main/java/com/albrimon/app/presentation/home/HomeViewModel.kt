package com.albrimon.app.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DesignServices
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.lifecycle.ViewModel
import com.albrimon.app.navigation.Routes
import com.albrimon.app.core.ui.theme.AzulAgua
import com.albrimon.app.core.ui.theme.NaranjaSuave
import com.albrimon.app.core.ui.theme.RosaEmpolvado
import com.albrimon.app.core.ui.theme.RosaSuave
import com.albrimon.app.core.ui.theme.VerdeSalvia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            modules = listOf(
                HomeModule("Clientes", "Gestiona sus medidas\ny proyectos", Icons.Outlined.PersonOutline, AzulAgua, Routes.CLIENTS),
                HomeModule("Proyectos", "Crea y consulta\ntus prendas", Icons.Outlined.FolderOpen, RosaSuave, Routes.PROJECTS),
                HomeModule("Patrones", "Calcula y diseña", Icons.Outlined.DesignServices, NaranjaSuave, Routes.soon("Patrones")),
                HomeModule("Materiales", "Telas y retazos", Icons.Outlined.Layers, VerdeSalvia, Routes.SCRAPS),
                HomeModule("Costos", "Presupuesto y venta", Icons.Outlined.Paid, RosaSuave, Routes.soon("Costos")),
                HomeModule("Nuevo proyecto", "Comienza a diseñar", Icons.Filled.Add, RosaEmpolvado, Routes.soon("Nuevo proyecto"))
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
