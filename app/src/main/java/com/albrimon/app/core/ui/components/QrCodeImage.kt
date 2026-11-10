package com.albrimon.app.core.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/** Convierte un texto en un código QR (matriz de ZXing -> Bitmap blanco y negro). */
fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap {
    val matrix = QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        sizePx,
        sizePx,
        mapOf(EncodeHintType.MARGIN to 1)
    )
    val pixels = IntArray(sizePx * sizePx) { index ->
        if (matrix[index % sizePx, index / sizePx]) Color.BLACK else Color.WHITE
    }
    return Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888)
}

/** Muestra un QR en Compose. Se genera una sola vez por contenido. */
@Composable
fun QrCodeImage(content: String, size: Dp = 200.dp, contentDescription: String? = null) {
    val bitmap = remember(content) { generateQrBitmap(content).asImageBitmap() }
    Image(bitmap = bitmap, contentDescription = contentDescription, modifier = Modifier.size(size))
}
