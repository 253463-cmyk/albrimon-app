package com.albrimon.app.presentation.scrap

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.albrimon.app.domain.model.Scrap
import com.albrimon.app.domain.cut.Placement
import com.albrimon.app.domain.cut.ScrapUse
import kotlin.math.min

/** Colores para distinguir piezas; el número dentro de cada pieza coincide con la leyenda. */
val PiecePalette = listOf(
    Color(0xFFE8C4B8), Color(0xFFC9D8E0), Color(0xFFDADFC7), Color(0xFFF4D3B8),
    Color(0xFFD9B8D6), Color(0xFFB8D9CF), Color(0xFFF0E0A8), Color(0xFFC4C4E8)
)

fun pieceColor(number: Int): Color = PiecePalette[(number - 1).mod(PiecePalette.size)]

/** Dibuja el retazo y dónde se corta cada pieza (con Canvas de Compose, sin librerías extra). */
@Composable
fun CutPlanCanvas(
    scrap: Scrap,
    placements: List<Placement>,
    use: ScrapUse?,
    pieceNumbers: Map<Long, Int>,
    modifier: Modifier = Modifier
) {
    val ratio = (scrap.widthMm.toFloat() / scrap.heightMm).coerceIn(0.5f, 3f)
    val base = parseColorOrGray(scrap.colorHex)

    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(ratio)) {
        val scale = min(size.width / scrap.widthMm, size.height / scrap.heightMm)
        val fullW = scrap.widthMm * scale
        val fullH = scrap.heightMm * scale
        val origin = Offset((size.width - fullW) / 2f, (size.height - fullH) / 2f)

        // Retazo
        drawRect(base.copy(alpha = 0.35f), topLeft = origin, size = Size(fullW, fullH))
        drawRect(Color(0xFF7A6A66), topLeft = origin, size = Size(fullW, fullH), style = Stroke(width = 2f))

        // Sobrante reutilizable (franja inferior) con línea punteada
        if (use != null && use.leftoverWmm > 0 && use.leftoverHmm > 0) {
            val lw = use.leftoverWmm * scale
            val lh = use.leftoverHmm * scale
            drawRect(
                Color(0xFF2B1B1B),
                topLeft = Offset(origin.x, origin.y + fullH - lh),
                size = Size(lw, lh),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
            )
        }

        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            color = android.graphics.Color.parseColor("#2B1B1B")
        }
        placements.forEach { p ->
            val number = pieceNumbers[p.pieceId] ?: 0
            val topLeft = Offset(origin.x + p.x * scale, origin.y + p.y * scale)
            val pieceSize = Size(p.w * scale, p.h * scale)
            drawRect(pieceColor(number), topLeft = topLeft, size = pieceSize)
            drawRect(Color(0xFF2B1B1B), topLeft = topLeft, size = pieceSize, style = Stroke(width = 2f))
            paint.textSize = min(pieceSize.width, pieceSize.height).coerceIn(18f, 56f) * 0.6f
            drawContext.canvas.nativeCanvas.drawText(
                number.toString(),
                topLeft.x + pieceSize.width / 2f,
                topLeft.y + pieceSize.height / 2f + paint.textSize / 3f,
                paint
            )
        }
    }
}
