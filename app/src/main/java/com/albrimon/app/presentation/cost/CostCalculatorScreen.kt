package com.albrimon.app.presentation.cost

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.albrimon.app.core.ui.components.AlbrimonScaffold
import com.albrimon.app.core.ui.theme.AccentFont
import java.text.NumberFormat
import java.util.Locale

private val pesos: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"))
private fun money(value: Double): String = pesos.format(value)

private val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)

@Composable
fun CostCalculatorScreen(
    navController: NavController,
    viewModel: CostViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val breakdown = state.breakdown

    AlbrimonScaffold(navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Calculadora de costos",
                        fontFamily = AccentFont,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Calcula el presupuesto de confección de una prenda.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = viewModel::reset) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reiniciar cálculo")
                }
            }

            // ---------- Materiales ----------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Materiales", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Button(onClick = viewModel::addMaterial) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar")
                        }
                    }
                    state.materials.forEachIndexed { index, item ->
                        MaterialRow(
                            index = index,
                            item = item,
                            canDelete = state.materials.size > 1,
                            onDelete = { viewModel.removeMaterial(item.id) },
                            onChange = { transform -> viewModel.updateMaterial(item.id, transform) }
                        )
                        if (index != state.materials.lastIndex) Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ---------- Mano de obra y porcentajes ----------
            OutlinedTextField(
                value = state.labor,
                onValueChange = viewModel::onLaborChange,
                label = { Text("Mano de obra ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = decimalKeyboard,
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.contingencyPct,
                    onValueChange = viewModel::onContingencyChange,
                    label = { Text("Imprevistos (%)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = decimalKeyboard,
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.taxPct,
                    onValueChange = viewModel::onTaxChange,
                    label = { Text("IVA (%)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = decimalKeyboard,
                    singleLine = true
                )
            }

            // ---------- Resumen ----------
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Resumen del presupuesto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    CostLine("Materiales", breakdown.materials)
                    CostLine("Mano de obra", breakdown.labor)
                    CostLine("Base", breakdown.base)
                    CostLine("Imprevistos (${state.contingencyPct.ifBlank { "0" }}%)", breakdown.contingency)
                    CostLine("IVA (${state.taxPct.ifBlank { "0" }}%)", breakdown.tax)
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total estimado", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(
                            money(breakdown.total),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Text(
                "Fórmula: (materiales + mano de obra) + imprevistos + IVA. Los porcentajes son editables.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MaterialRow(
    index: Int,
    item: MaterialInput,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onChange: ((MaterialInput) -> MaterialInput) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Material ${index + 1}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            IconButton(onClick = onDelete, enabled = canDelete) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar material")
            }
        }
        OutlinedTextField(
            value = item.name,
            onValueChange = { value -> onChange { it.copy(name = value) } },
            label = { Text("Nombre (ej. tela)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = item.quantity,
                onValueChange = { value -> onChange { it.copy(quantity = value) } },
                label = { Text("Cantidad") },
                modifier = Modifier.weight(1f),
                keyboardOptions = decimalKeyboard,
                singleLine = true
            )
            OutlinedTextField(
                value = item.unit,
                onValueChange = { value -> onChange { it.copy(unit = value) } },
                label = { Text("Unidad") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        OutlinedTextField(
            value = item.unitPrice,
            onValueChange = { value -> onChange { it.copy(unitPrice = value) } },
            label = { Text("Precio por unidad ($)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = decimalKeyboard,
            singleLine = true
        )
        Text(
            "Subtotal: ${money(item.subtotal)}",
            modifier = Modifier.align(Alignment.End),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CostLine(label: String, value: Double) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Text(money(value), fontSize = 13.sp)
    }
}
