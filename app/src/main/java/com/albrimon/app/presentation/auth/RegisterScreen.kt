package com.albrimon.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.activity.compose.rememberLauncherForActivityResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.albrimon.app.core.ui.components.AlbrimonLoadingScreen
import com.albrimon.app.core.ui.components.AlbrimonLogo
import com.albrimon.app.core.ui.components.AlbrimonTextField
import com.albrimon.app.core.ui.components.PrimaryButton
import com.albrimon.app.core.ui.theme.AccentFont

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Escáner QR (ZXing): abre la cámara, pide el permiso si hace falta y devuelve el texto leído
    val qrScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let(viewModel::onLinkCodeChange)
    }

    // Mientras responde la API se muestra la pantalla de carga
    if (state.isLoading) {
        AlbrimonLoadingScreen(message = "Creando tu cuenta...")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AlbrimonLogo(modifier = Modifier.align(Alignment.CenterHorizontally), nameSize = 40)
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Crear cuenta", fontFamily = AccentFont, fontSize = 24.sp)
        Text(
            text = "Únete a Albrimon y comienza a gestionar tu taller de forma fácil y organizada",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AlbrimonTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            placeholder = "Nombre completo",
            leadingIcon = Icons.Filled.Person
        )
        AlbrimonTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Correo electrónico",
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email
        )
        AlbrimonTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Contraseña",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            passwordVisible = state.passwordVisible,
            onTogglePassword = viewModel::togglePasswordVisibility
        )

        // Opcional: si el taller ya la registró como cliente, el código conecta su cuenta
        AlbrimonTextField(
            value = state.linkCode,
            onValueChange = viewModel::onLinkCodeChange,
            placeholder = "Código de vínculo (opcional)",
            leadingIcon = Icons.Filled.QrCodeScanner
        )
        OutlinedButton(
            onClick = {
                qrScanner.launch(
                    ScanOptions()
                        .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        .setPrompt("Escanea el QR que te muestra tu diseñador")
                        .setBeepEnabled(false)
                        .setOrientationLocked(false)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escanear QR de mi taller")
        }

        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        PrimaryButton(text = "Registrarse", onClick = { viewModel.register(onRegisterSuccess) })

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Ya tienes una cuenta?",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onGoToLogin) {
                Text(
                    text = "Iniciar sesión",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}
