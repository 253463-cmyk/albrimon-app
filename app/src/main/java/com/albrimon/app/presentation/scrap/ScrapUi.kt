package com.albrimon.app.presentation.scrap

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.albrimon.app.core.ui.theme.AccentFont
import java.util.Locale

/** Formatea milímetros como centímetros: 700 -> "70", 455 -> "45.5". */
fun mmToCm(mm: Int): String {
    val cm = mm / 10.0
    return if (mm % 10 == 0) (mm / 10).toString() else String.format(Locale.US, "%.1f", cm)
}

fun sizeLabel(wMm: Int, hMm: Int) = "${mmToCm(wMm)} × ${mmToCm(hMm)} cm"

/** "#RRGGBB" -> Color; si el valor no es válido devuelve gris para no romper la pantalla. */
fun parseColorOrGray(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)

/** Pantalla secundaria con flecha de regreso (formulario y plan de corte). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontFamily = AccentFont) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = content
    )
}
