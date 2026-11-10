package com.albrimon.app.presentation.scrap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.albrimon.app.domain.cut.CutPlan

@Composable
fun CutPlanScreen(
    navController: NavController,
    viewModel: CutPlanViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }

    SubScreenScaffold(title = "Plan de corte", onBack = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- 1. Proyecto ----------
            Text("1. Proyecto", fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    enabled = !state.loadingProjects && state.projects.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            state.loadingProjects -> "Cargando proyectos..."
                            state.projects.isEmpty() -> "No hay proyectos"
                            else -> state.selected?.let { "${it.name} · ${it.clientName.orEmpty()}" } ?: "Elegir proyecto"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    state.projects.forEach { project ->
                        DropdownMenuItem(
                            text = { Text("${project.name} · ${project.clientName.orEmpty()}") },
                            onClick = {
                                menuOpen = false
                                viewModel.selectProject(project)
                            }
                        )
                    }
                }
            }
            if (!state.loadingProjects && state.projects.isEmpty() && state.error == null) {
                NoticeCard("Todavía no hay proyectos. Crea uno (con sus piezas de patronaje) y vuelve aquí.")
            }

            state.error?.let {
                ErrorBanner(
                    message = it,
                    onRetry = {
                        if (state.selected == null) viewModel.loadProjects() else viewModel.retryPieces()
                    }
                )
            }
            state.success?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) { Text(it, modifier = Modifier.padding(14.dp), fontSize = 14.sp) }
            }

            // ---------- 2. Piezas ----------
            if (state.selected != null) {
                Text("2. Piezas del proyecto", fontWeight = FontWeight.SemiBold)
                when {
                    state.loadingPieces -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                    state.pieces.isEmpty() -> NoticeCard("Este proyecto no tiene piezas de patronaje registradas.")
                    else -> Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.pieces.forEachIndexed { index, piece ->
                                Row {
                                    Text("${index + 1}. ${piece.name}", modifier = Modifier.weight(1f), fontSize = 14.sp)
                                    Text(
                                        sizeLabel(piece.widthMm, piece.heightMm) + if (piece.canRotate) " ↻" else "",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Text(
                                "↻ = la pieza puede girarse 90°",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    "Retazos disponibles: ${state.scraps.size}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = viewModel::calculate,
                    enabled = !state.calculating && !state.applying && state.pieces.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Calcular acomodo") }
            }

            // ---------- 3. Resultado ----------
            state.plan?.let { plan ->
                PlanResult(state, plan)
                Button(
                    onClick = viewModel::apply,
                    enabled = plan.isComplete && !state.applying,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (state.applying) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Aplicar plan al inventario")
                    }
                }
                if (!plan.isComplete) {
                    Text(
                        "No se puede aplicar mientras haya piezas sin acomodar.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---------- 4. Historial ----------
            if (state.selected != null) {
                HorizontalDivider()
                Text("Historial de retazos usados", fontWeight = FontWeight.SemiBold)
                if (state.history.isEmpty()) {
                    Text("Aún no se ha usado ningún retazo en este proyecto.", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.history.forEach { usage ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(usage.scrapName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    "Área usada: ${"%.0f".format(usage.usedAreaMm2 / 100.0)} cm²" +
                                        if (usage.leftoverWmm > 0 && usage.leftoverHmm > 0) {
                                            " · Sobrante: ${sizeLabel(usage.leftoverWmm, usage.leftoverHmm)}"
                                        } else " · Sin sobrante útil",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanResult(state: CutPlanUiState, plan: CutPlan) {
    Text("3. Resultado", fontWeight = FontWeight.SemiBold)

    // Número de pieza = posición en la lista, igual que en "Piezas del proyecto".
    val numbers = state.pieces.mapIndexed { index, p -> p.id to index + 1 }.toMap()
    val scrapsById = state.scraps.associateBy { it.id }

    val placedCount = plan.placements.size
    Text(
        if (plan.isComplete) "Las ${state.pieces.size} piezas caben usando ${plan.uses.size} retazo(s)."
        else "Se acomodaron $placedCount de ${state.pieces.size} piezas.",
        fontSize = 14.sp,
        color = if (plan.isComplete) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
    )

    plan.uses.forEach { use ->
        val scrap = scrapsById[use.scrapId] ?: return@forEach
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${scrap.name} · ${sizeLabel(scrap.widthMm, scrap.heightMm)}", fontWeight = FontWeight.SemiBold)
                CutPlanCanvas(
                    scrap = scrap,
                    placements = plan.placements.filter { it.scrapId == scrap.id },
                    use = use,
                    pieceNumbers = numbers
                )
                Text("Aprovechamiento: ${"%.0f".format(use.usagePct)} %", fontSize = 13.sp)
                Text(
                    if (use.leftoverWmm > 0) {
                        "Sobrante reutilizable: ${sizeLabel(use.leftoverWmm, use.leftoverHmm)} (línea punteada). Se guardará como nuevo retazo."
                    } else {
                        "Sin sobrante reutilizable (menos de 10 × 10 cm)."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                plan.placements.filter { it.scrapId == scrap.id }.forEach { p ->
                    val n = numbers[p.pieceId] ?: 0
                    val name = state.pieces.firstOrNull { it.id == p.pieceId }?.name.orEmpty()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(16.dp).background(pieceColor(n), RoundedCornerShape(3.dp)))
                        Text(
                            "$n. $name — ${sizeLabel(p.w, p.h)}" + if (p.rotated) " (girada)" else "",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    if (plan.unplaced.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Sin acomodar (no caben en ningún retazo disponible):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer)
                plan.unplaced.forEach {
                    Text("• ${it.name} (${sizeLabel(it.w, it.h)})", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}
