package com.albrimon.app.presentation.scrap

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.albrimon.app.data.local.scrapPhotoFile
import com.albrimon.app.core.ui.theme.Dorado

private val FabricOptions = listOf("Algodón", "Mezclilla", "Lino", "Poliéster", "Seda", "Lana", "Otro")
private val ColorPresets = listOf(
    "#F5EBE0", "#E8C4B8", "#8E3B3F", "#C9D8E0", "#DADFC7", "#2B1B1B", "#C9A66B", "#3F5E8C"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrapFormScreen(
    navController: NavController,
    viewModel: ScrapFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.done) {
        if (state.done) navController.popBackStack()
    }

    // --- Cámara: TakePicture escribe en un archivo privado expuesto con FileProvider ---
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        viewModel.onCameraResult(ok)
    }
    fun launchCamera() {
        val photo = viewModel.prepareCapture() ?: return
        try {
            cameraLauncher.launch(photo.uri)
        } catch (e: ActivityNotFoundException) {
            viewModel.onCameraResult(false)
            viewModel.onPhotoMessage("No hay una app de cámara disponible. Elige una foto de la galería.")
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            val activity = context as? Activity
            val canAskAgain = activity != null &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
            viewModel.onCameraPermissionDenied(blocked = !canAskAgain)
        }
    }
    val hasCamera = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }
    fun onCameraClick() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) launchCamera() else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // --- Galería: selector del sistema, no necesita permisos ---
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onGalleryPicked(uri)
    }

    SubScreenScaffold(
        title = when {
            state.readOnly -> "Retazo usado"
            state.isEditing -> "Editar retazo"
            else -> "Nuevo retazo"
        },
        onBack = { navController.popBackStack() }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@SubScreenScaffold
        }
        val editable = !state.readOnly && !state.isSaving

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.readOnly) {
                NoticeCard("Este retazo ya se usó en un plan de corte, por eso no se puede editar ni eliminar. Consulta el historial en el plan del proyecto.")
            }

            // ---------------- Foto ----------------
            val photoFile = scrapPhotoFile(context, state.photoPath)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(parseColorOrGray(state.colorHex).copy(alpha = 0.35f))
                    .border(BorderStroke(1.dp, Dorado.copy(alpha = 0.6f)), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (photoFile != null) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = "Foto del retazo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Sin foto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (editable) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasCamera) {
                        OutlinedButton(onClick = { onCameraClick() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.CameraAlt, null, Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Cámara")
                        }
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, null, Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Galería")
                    }
                    if (state.photoPath != null) {
                        OutlinedButton(onClick = viewModel::removePhoto) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar foto", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            state.photoMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                if (state.cameraPermissionBlocked) {
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        )
                    }) { Text("Abrir ajustes de la app") }
                }
            }

            // ---------------- Datos ----------------
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = { Text("Nombre") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                singleLine = true,
                enabled = editable,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de tela", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FabricOptions.forEach { option ->
                    FilterChip(
                        selected = state.fabricType.equals(option, ignoreCase = true),
                        onClick = { viewModel.onFabric(option) },
                        enabled = editable,
                        label = { Text(option) }
                    )
                }
            }
            if (state.fabricType == "Otro" || (state.fabricType.isNotBlank() && state.fabricType !in FabricOptions)) {
                OutlinedTextField(
                    value = state.fabricType,
                    onValueChange = viewModel::onFabric,
                    label = { Text("Otro tipo de tela") },
                    singleLine = true,
                    enabled = editable,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            state.fabricError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.widthCm,
                    onValueChange = viewModel::onWidth,
                    label = { Text("Ancho (cm)") },
                    isError = state.widthError != null,
                    supportingText = state.widthError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = editable,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.heightCm,
                    onValueChange = viewModel::onHeight,
                    label = { Text("Alto (cm)") },
                    isError = state.heightError != null,
                    supportingText = state.heightError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = editable,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Color", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorPresets.forEach { hex ->
                    val selected = state.colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(parseColorOrGray(hex))
                            .border(
                                BorderStroke(if (selected) 3.dp else 1.dp, if (selected) Dorado else Color.Gray.copy(alpha = 0.4f)),
                                CircleShape
                            )
                            .clickable(enabled = editable) { viewModel.onColor(hex) }
                    )
                }
            }
            OutlinedTextField(
                value = state.colorHex,
                onValueChange = viewModel::onColor,
                label = { Text("Código de color (#RRGGBB)") },
                isError = state.colorError != null,
                supportingText = state.colorError?.let { { Text(it) } },
                singleLine = true,
                enabled = editable,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tiene hilo o estampado direccional", fontSize = 15.sp)
                    Text(
                        "Si está activo, las piezas no se giran 90° al acomodarlas.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.hasGrain, onCheckedChange = viewModel::onGrain, enabled = editable)
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            if (!state.readOnly) {
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (state.isEditing) "Guardar cambios" else "Registrar retazo")
                    }
                }
                if (state.isEditing) {
                    OutlinedButton(
                        onClick = { confirmDelete = true },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Eliminar retazo", color = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("¿Eliminar este retazo?") },
            text = { Text("Se borrará del inventario y su foto del teléfono. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.delete() }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
        )
    }
}
