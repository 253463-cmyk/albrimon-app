package com.albrimon.app.presentation.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.albrimon.app.data.repository.formatDueDate
import com.albrimon.app.domain.model.Client
import com.albrimon.app.navigation.Routes
import com.albrimon.app.presentation.scrap.SubScreenScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    navController: NavController,
    viewModel: ProjectFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    SubScreenScaffold(title = "Nuevo proyecto", onBack = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre de la prenda *") },
                placeholder = { Text("Ej. Vestido midi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ClientSelector(
                clients = state.clients,
                loading = state.loadingClients,
                selected = state.selectedClient,
                onSelect = viewModel::onClientSelected
            )
            if (!state.loadingClients && state.clients.isEmpty()) {
                TextButton(onClick = { navController.navigate(Routes.CLIENT_FORM) }) {
                    Text("No tienes clientes activos. Registrar cliente")
                }
            }

            // Campo de solo lectura: al tocarlo se abre el calendario
            Box {
                OutlinedTextField(
                    value = state.dueDate?.let { formatDueDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de entrega") },
                    placeholder = { Text("Sin fecha") },
                    trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notas") },
                placeholder = { Text("Tela, color, detalles...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }

            Button(
                onClick = {
                    viewModel.save { projectId ->
                        // Abre el detalle del proyecto nuevo y saca el formulario de la pila
                        navController.navigate(Routes.projectDetail(projectId)) {
                            popUpTo(Routes.PROJECT_FORM) { inclusive = true }
                        }
                    }
                },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear proyecto")
                }
            }
            Text(
                "Después de crearlo podrás agregar las piezas de patronaje.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDueDateSelected(pickerState.selectedDateMillis?.let(::millisToIso))
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/** El DatePicker entrega milisegundos en UTC; se convierte a "2026-12-12". */
private fun millisToIso(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millis))

@Composable
private fun ClientSelector(
    clients: List<Client>,
    loading: Boolean,
    selected: Client?,
    onSelect: (Client) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cliente *") },
            placeholder = { Text(if (loading) "Cargando clientes..." else "Selecciona un cliente") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable(enabled = !loading && clients.isNotEmpty()) { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            clients.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.name) },
                    onClick = {
                        expanded = false
                        onSelect(client)
                    }
                )
            }
        }
    }
}
