package com.albrimon.app.presentation.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.albrimon.app.presentation.scrap.SubScreenScaffold

private val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)

@Composable
fun ClientFormScreen(
    navController: NavController,
    viewModel: ClientFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SubScreenScaffold(title = "Nuevo cliente", onBack = { navController.popBackStack() }) { padding ->
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
                onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                label = { Text("Nombre completo *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.phone,
                onValueChange = { v -> viewModel.update { it.copy(phone = v) } },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.age,
                    onValueChange = { v -> viewModel.update { it.copy(age = v) } },
                    label = { Text("Edad") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = numberKeyboard,
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.size,
                    onValueChange = { v -> viewModel.update { it.copy(size = v) } },
                    label = { Text("Talla (S, M, L...)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Text("Medidas corporales (cm)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MeasureField("Busto", state.bust, Modifier.weight(1f)) { v -> viewModel.update { it.copy(bust = v) } }
                MeasureField("Cintura", state.waist, Modifier.weight(1f)) { v -> viewModel.update { it.copy(waist = v) } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MeasureField("Cadera", state.hip, Modifier.weight(1f)) { v -> viewModel.update { it.copy(hip = v) } }
                MeasureField("Largo", state.length, Modifier.weight(1f)) { v -> viewModel.update { it.copy(length = v) } }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Button(
                onClick = { viewModel.save(onSaved = { navController.popBackStack() }) },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar cliente")
                }
            }
            Text(
                "* Campo obligatorio. Las demás medidas se pueden completar después.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MeasureField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = numberKeyboard,
        singleLine = true
    )
}
