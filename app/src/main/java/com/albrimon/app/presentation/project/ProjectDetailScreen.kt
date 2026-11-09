package com.albrimon.app.presentation.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.albrimon.app.core.ui.components.ErrorBox
import com.albrimon.app.core.ui.components.LoadingBox
import com.albrimon.app.domain.model.PatternPiece
import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus
import com.albrimon.app.navigation.Routes
import com.albrimon.app.presentation.scrap.SubScreenScaffold
import com.albrimon.app.presentation.scrap.sizeLabel
import kotlinx.coroutines.delay

@Composable
fun ProjectDetailScreen(
    navController: NavController,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddPiece by remember { mutableStateOf(false) }

    // El aviso de éxito se oculta solo después de unos segundos
    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(2_500)
            viewModel.clearMessage()
        }
    }

    SubScreenScaffold(
        title = state.project?.name ?: "Proyecto",
        onBack = { navController.popBackStack() }
    ) { padding ->
        val project = state.project
        val error = state.error
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.isLoading) {
                LoadingBox()
                return@Column
            }
            if (project == null) {
                ErrorBox(message = error ?: "Proyecto no encontrado", onRetry = viewModel::refresh)
                return@Column
            }

            if (state.isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp) }

            ProjectInfoCard(
                project = project,
                enabled = !state.isBusy,
                onStatusChange = viewModel::changeStatus
            )

            PiecesCard(
                pieces = state.pieces,
                enabled = !state.isBusy,
                onAdd = { showAddPiece = true },
                onDelete = viewModel::deletePiece
            )

            Button(
                onClick = { navController.navigate(Routes.scrapPlan(project.id)) },
                enabled = state.pieces.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.ContentCut, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Plan de corte con retazos")
            }
            if (state.pieces.isEmpty()) {
                Text(
                    "Agrega al menos una pieza para calcular el plan de corte.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showAddPiece) {
        AddPieceDialog(
            onDismiss = { showAddPiece = false },
            onConfirm = { name, width, height, rotate ->
                val validation = viewModel.addPiece(name, width, height, rotate)
                if (validation == null) showAddPiece = false
                validation
            }
        )
    }
}

@Composable
private fun ProjectInfoCard(
    project: Project,
    enabled: Boolean,
    onStatusChange: (ProjectStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoLine("Cliente", project.clientName)
            InfoLine("Entrega", project.date)
            if (!project.notes.isNullOrBlank()) InfoLine("Notas", project.notes)
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Estado", modifier = Modifier.weight(1f), fontSize = 14.sp)
                StatusSelector(project.status, enabled, onStatusChange)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row {
        Text(
            "$label: ",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusSelector(
    current: ProjectStatus,
    enabled: Boolean,
    onSelect: (ProjectStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled) {
            Text(current.label)
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Cambiar estado")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ProjectStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label) },
                    onClick = {
                        expanded = false
                        onSelect(status)
                    }
                )
            }
        }
    }
}

@Composable
private fun PiecesCard(
    pieces: List<PatternPiece>,
    enabled: Boolean,
    onAdd: () -> Unit,
    onDelete: (PatternPiece) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Piezas de patronaje (${pieces.size})",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onAdd, enabled = enabled) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Agregar")
                }
            }
            if (pieces.isEmpty()) {
                Text(
                    "Este proyecto todavía no tiene piezas.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            pieces.forEach { piece ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(piece.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            sizeLabel(piece.widthMm, piece.heightMm) +
                                if (piece.canRotate) "  ·  se puede girar" else "  ·  sin girar",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDelete(piece) }, enabled = enabled) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar ${piece.name}")
                    }
                }
            }
        }
    }
}

/** onConfirm devuelve un mensaje de error de validación, o null si se aceptó. */
@Composable
private fun AddPieceDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, widthCm: String, heightCm: String, canRotate: Boolean) -> String?
) {
    var name by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var canRotate by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva pieza") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nombre (ej. delantero)") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = width,
                        onValueChange = { width = it; error = null },
                        label = { Text("Ancho (cm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = decimal,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it; error = null },
                        label = { Text("Alto (cm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = decimal,
                        singleLine = true
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canRotate, onCheckedChange = { canRotate = it })
                    Text("Se puede girar al cortar", fontSize = 14.sp)
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(onClick = { error = onConfirm(name, width, height, canRotate) }) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
