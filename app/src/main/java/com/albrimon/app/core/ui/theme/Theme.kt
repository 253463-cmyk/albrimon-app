package com.albrimon.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RojoVinoClaro,
    onPrimary = Color.White,
    primaryContainer = RosaEmpolvado,
    onPrimaryContainer = RojoVino,
    secondary = Dorado,
    onSecondary = Color.White,
    secondaryContainer = NaranjaSuave,
    onSecondaryContainer = TextoOscuro,
    tertiary = RojoVino,
    background = Beige,
    onBackground = TextoOscuro,
    surface = BeigeClaro,
    onSurface = TextoOscuro,
    surfaceVariant = BeigeOscuro,
    onSurfaceVariant = TextoSuave,
    outline = Dorado
)

private val DarkColorScheme = darkColorScheme(
    primary = OscuroPrimario,
    onPrimary = Color(0xFF3A1114),
    primaryContainer = RojoVino,
    onPrimaryContainer = RosaSuave,
    secondary = Dorado,
    onSecondary = Color(0xFF2B1B1B),
    secondaryContainer = Color(0xFF4A3A2C),
    onSecondaryContainer = RosaSuave,
    tertiary = RosaEmpolvado,
    background = OscuroFondo,
    onBackground = Color(0xFFF3E6DD),
    surface = OscuroSuperficie,
    onSurface = Color(0xFFF3E6DD),
    surfaceVariant = OscuroSuperficieVar,
    onSurfaceVariant = Color(0xFFCDB9B0),
    outline = Dorado
)

@Composable
fun AlbrimonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
