package com.albrimon.app.presentation.scrap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.albrimon.app.data.local.scrapPhotoFile
import com.albrimon.app.domain.model.Scrap
import com.albrimon.app.navigation.Routes
import com.albrimon.app.core.ui.components.AlbrimonScaffold
import com.albrimon.app.core.ui.components.FilterPills
import com.albrimon.app.core.ui.theme.AccentFont
import com.albrimon.app.core.ui.theme.Dorado
import com.albrimon.app.core.ui.theme.VerdeFiltro

@Composable
fun ScrapListScreen(
    navController: NavController,
    viewModel: ScrapListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AlbrimonScaffold(navController = navController) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Retazos", fontFamily = AccentFont, fontSize = 26.sp)
                        Text(
                            "Inventario de telas sobrantes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar inventario")
                    }
                }

                if (!state.isDesigner) {
                    NoticeCard(
                        "Retazos es una herramienta del diseñador. Inicia sesión con una cuenta de diseñador para usarla."
                    )
                    return@Column
                }

                FilterPills(
                    options = listOf("Disponibles", "Todos"),
                    selectedIndex = if (state.onlyAvailable) 0 else 1,
                    onSelect = { viewModel.setOnlyAvailable(it == 0) }
                )

                Button(
                    onClick = { navController.navigate(Routes.SCRAP_PLAN) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.scraps.any { it.available }
                ) {
                    Icon(Icons.Filled.ContentCut, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Planificar corte de un proyecto")
                }

                state.error?.let { message ->
                    ErrorBanner(
                        message = message,
                        showCacheNote = state.scraps.isNotEmpty() && state.isOffline,
                        onRetry = viewModel::refresh,
                        onLogin = if (state.sessionExpired) {
                            {
                                viewModel.logout {
                                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                                }
                            }
                        } else null
                    )
                }

                when {
                    state.isLoading && state.scraps.isEmpty() -> Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                    state.scraps.isEmpty() && state.error == null -> NoticeCard(
                        if (state.onlyAvailable) {
                            "Aún no hay retazos disponibles. Toca + para registrar el primero, con foto."
                        } else {
                            "No hay retazos registrados."
                        }
                    )

                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.scraps, key = { it.id }) { scrap ->
                            ScrapCard(scrap) { navController.navigate(Routes.scrapForm(scrap.id)) }
                        }
                        item { Spacer(Modifier.size(72.dp)) }
                    }
                }
            }

            if (state.isDesigner) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.scrapForm(0L)) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(Icons.Filled.Add, contentDescription = "Registrar retazo") }
            }
        }
    }
}

@Composable
private fun ScrapCard(scrap: Scrap, onClick: () -> Unit) {
    val context = LocalContext.current
    val photoFile = scrapPhotoFile(context, scrap.photoPath)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(parseColorOrGray(scrap.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                if (photoFile != null) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = "Foto de ${scrap.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = "Sin foto",
                        tint = swatchIconTint(scrap.colorHex)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(scrap.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(
                    "${scrap.fabricType} · ${sizeLabel(scrap.widthMm, scrap.heightMm)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (scrap.hasGrain) {
                    Text(
                        "Con hilo/estampado: no se gira",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusChip(scrap.available)
        }
    }
}

/** Icono legible sobre el color del retazo (oscuro sobre claros, claro sobre oscuros). */
private fun swatchIconTint(hex: String): Color {
    val c = parseColorOrGray(hex)
    val luminance = 0.299f * c.red + 0.587f * c.green + 0.114f * c.blue
    return if (luminance > 0.6f) Color(0x99000000) else Color(0xCCFFFFFF)
}

@Composable
private fun StatusChip(available: Boolean) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (available) VerdeFiltro else MaterialTheme.colorScheme.outlineVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            if (available) "Disponible" else "Usado",
            fontSize = 11.sp,
            color = Color(0xFF2B1B1B)
        )
    }
}

@Composable
fun NoticeCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Dorado.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Text(text, modifier = Modifier.padding(16.dp), fontSize = 14.sp)
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showCacheNote: Boolean = false,
    onLogin: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            if (showCacheNote) {
                Text(
                    "Mostrando el último inventario guardado en tu teléfono.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRetry) { Text("Reintentar") }
                if (onLogin != null) OutlinedButton(onClick = onLogin) { Text("Iniciar sesión") }
            }
        }
    }
}
