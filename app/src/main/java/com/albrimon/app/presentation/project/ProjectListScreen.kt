package com.albrimon.app.presentation.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.albrimon.app.domain.model.ProjectStatus
import com.albrimon.app.navigation.Routes
import com.albrimon.app.core.ui.components.AlbrimonScaffold
import com.albrimon.app.core.ui.components.ErrorBox
import com.albrimon.app.core.ui.components.LoadingBox
import com.albrimon.app.core.ui.components.NotificationBell
import com.albrimon.app.core.ui.components.ProjectCard
import com.albrimon.app.core.ui.components.SearchField
import com.albrimon.app.core.ui.theme.TextoOscuro
import com.albrimon.app.core.ui.theme.VerdeFiltro
import com.albrimon.app.core.ui.theme.VerdeFiltroClaro

@Composable
fun ProjectListScreen(
    navController: NavController,
    viewModel: ProjectListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Se recarga al entrar y al volver de un detalle o formulario
    LaunchedEffect(Unit) { viewModel.refresh() }

    AlbrimonScaffold(navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.clientName?.let { "Proyectos de $it" } ?: "Proyectos",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { navController.navigate(Routes.PROJECT_FORM) }) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Nuevo proyecto",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                NotificationBell()
            }
            Spacer(modifier = Modifier.height(12.dp))

            SearchField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Buscar proyecto..."
            )
            Spacer(modifier = Modifier.height(12.dp))

            StatusDropdown(
                selected = state.selectedStatus,
                onSelected = viewModel::onStatusSelected
            )
            Spacer(modifier = Modifier.height(12.dp))

            val error = state.error
            if (state.isLoading) {
                LoadingBox()
            } else if (error != null) {
                ErrorBox(message = error, onRetry = viewModel::refresh)
            } else if (state.projects.isEmpty()) {
                Text(
                    text = "No se encontraron proyectos",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.projects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { navController.navigate(Routes.projectDetail(project.id)) }
                        )
                    }
                }
            }
        }
    }
}

/** Filtro por estado: Todo / Pendiente / En proceso / Entregado / Terminado / Cancelado. */
@Composable
private fun StatusDropdown(
    selected: ProjectStatus?,
    onSelected: (ProjectStatus?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selected?.label ?: "Todo"

    Box {
        Surface(
            modifier = Modifier.clickable { expanded = true },
            shape = RoundedCornerShape(6.dp),
            color = VerdeFiltro
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, fontSize = 12.sp, color = TextoOscuro)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Filtrar por estado",
                    tint = TextoOscuro
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = VerdeFiltroClaro
        ) {
            DropdownMenuItem(
                text = { Text("Todo", fontSize = 12.sp, color = TextoOscuro) },
                onClick = { onSelected(null); expanded = false },
                modifier = Modifier.background(if (selected == null) VerdeFiltro else Color.Transparent)
            )
            ProjectStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label, fontSize = 12.sp, color = TextoOscuro) },
                    onClick = { onSelected(status); expanded = false },
                    modifier = Modifier.background(if (selected == status) VerdeFiltro else Color.Transparent)
                )
            }
        }
    }
}
