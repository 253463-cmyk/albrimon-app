package com.albrimon.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.albrimon.app.core.ui.components.AlbrimonScaffold
import com.albrimon.app.core.ui.components.InitialsAvatar
import com.albrimon.app.core.ui.components.NotificationBell
import com.albrimon.app.core.ui.theme.AccentFont
import com.albrimon.app.core.ui.theme.Dorado
import com.albrimon.app.core.ui.theme.RosaEmpolvado
import com.albrimon.app.core.ui.theme.TextoOscuro
import com.albrimon.app.core.ui.theme.VerdeSalvia

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AlbrimonScaffold(navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Hola, ${state.userName}", fontFamily = AccentFont, fontSize = 24.sp)
                    Text(
                        text = "Hoy es un gran día para crear algo increíble.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                NotificationBell()
                Spacer(modifier = Modifier.size(8.dp))
                InitialsAvatar(name = state.userName, size = 40.dp)
            }

            // Banner con frase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(RosaEmpolvado, VerdeSalvia))),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = state.quote,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(end = 16.dp),
                    fontFamily = AccentFont,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    color = TextoOscuro
                )
            }

            // Módulos (cuadrícula de 2 columnas)
            state.modules.chunked(2).forEach { rowModules ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowModules.forEach { module ->
                        ModuleTile(
                            module = module,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(module.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleTile(module: HomeModule, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(112.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = module.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = module.icon,
                contentDescription = module.title,
                tint = TextoOscuro,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = module.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
            )
            Text(
                text = module.subtitle,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                textAlign = TextAlign.Center,
                color = TextoOscuro.copy(alpha = 0.7f)
            )
        }
    }
}
