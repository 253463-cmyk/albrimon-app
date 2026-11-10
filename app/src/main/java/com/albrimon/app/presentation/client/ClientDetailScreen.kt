package com.albrimon.app.presentation.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.albrimon.app.navigation.Routes
import com.albrimon.app.core.ui.components.AlbrimonScaffold
import com.albrimon.app.core.ui.components.ErrorBox
import com.albrimon.app.core.ui.components.InitialsAvatar
import com.albrimon.app.core.ui.components.LoadingBox
import com.albrimon.app.core.ui.components.QrCodeImage

@Composable
fun ClientDetailScreen(
    clientId: Long,
    navController: NavController,
    viewModel: ClientDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(clientId) { viewModel.load(clientId) }

    AlbrimonScaffold(navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
            }

            val client = state.client
            val error = state.error
            if (state.isLoading) {
                LoadingBox()
            } else if (error != null) {
                ErrorBox(message = error, onRetry = { viewModel.load(clientId) })
            } else if (client == null) {
                Text(text = "Cliente no encontrado", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = client.name, size = 56.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = client.name, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = client.summary,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                InfoRow(
                    icon = Icons.Filled.Call,
                    title = "Teléfono: ${client.phone.ifBlank { "sin registrar" }}",
                    subtitle = null,
                    trailing = null,
                    onClick = null
                )

                InfoRow(
                    icon = Icons.Outlined.Straighten,
                    title = "Medidas corporales",
                    subtitle = "Busto, cintura, cadera, largo...",
                    trailing = if (state.measurementsExpanded) Icons.Filled.KeyboardArrowDown
                    else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    onClick = viewModel::toggleMeasurements
                )
                if (state.measurementsExpanded) {
                    val m = client.measurements
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MeasureLine("Busto", m.bust)
                            MeasureLine("Cintura", m.waist)
                            MeasureLine("Cadera", m.hip)
                            MeasureLine("Largo", m.length)
                        }
                    }
                }

                InfoRow(
                    icon = Icons.Outlined.FolderOpen,
                    title = "Proyectos relacionados",
                    subtitle = "${state.relatedProjects} proyectos",
                    trailing = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    onClick = { navController.navigate(Routes.clientProjects(clientId)) }
                )

                LinkAccountCard(linkCode = client.linkCode)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MeasureLine(label: String, value: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = if (value > 0) "$value cm" else "—", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    trailing: ImageVector?,
    onClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailing != null) {
                Icon(imageVector = trailing, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * QR con el código de vínculo del cliente. La clienta lo escanea al crear su cuenta
 * (pantalla Registro) y así puede ver el avance de sus proyectos desde su celular.
 * El backend borra el código cuando ya se usó, por eso null = cuenta ya vinculada.
 */
@Composable
private fun LinkAccountCard(linkCode: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Vincular cuenta del cliente", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (linkCode.isNullOrBlank()) {
                Text(
                    text = "✓ Este cliente ya vinculó su cuenta y puede ver sus proyectos.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                QrCodeImage(content = linkCode, size = 180.dp, contentDescription = "Código QR de vínculo")
                Text(text = linkCode, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Pide al cliente que lo escanee en \"Crear cuenta\" para ver sus proyectos.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
