package com.albrimon.app.ui.screens.auth

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albrimon.app.ui.components.AlbrimonLogo
import com.albrimon.app.ui.components.AlbrimonTextField
import com.albrimon.app.ui.components.PrimaryButton
import com.albrimon.app.ui.theme.AccentFont
import com.albrimon.app.viewmodel.auth.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
