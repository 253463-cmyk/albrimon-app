package com.albrimon.app.viewmodel.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class HomeModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

data class HomeUiState(
    val userName: String = "Valeria",
    val quote: String = "La moda también es una forma de cuidar el mundo.",
    val modules: List<HomeModule> = emptyList()
)
