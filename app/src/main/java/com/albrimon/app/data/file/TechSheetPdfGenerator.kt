package com.albrimon.app.data.file

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.albrimon.app.domain.model.Client
import com.albrimon.app.domain.model.PatternPiece
import com.albrimon.app.domain.model.Project
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Genera la ficha técnica de un proyecto en PDF con la API nativa de Android (PdfDocument),
 * sin librerías externas. El archivo queda en cache/pdfs y se comparte con FileProvider.
 */
@Singleton
class TechSheetPdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Tamaño carta en puntos (72 por pulgada)
    private val pageWidth = 612
    private val pageHeight = 792
    private val margin = 48f

    private val rose = Color.rgb(181, 103, 125)
    private val gray = Color.rgb(90, 90, 90)

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = rose; textSize = 26f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = rose; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = gray; textSize = 11f
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK; textSize = 11f; typeface = Typeface.DEFAULT_BOLD
    }
    private val linePaint = Paint().apply { color = Color.rgb(220, 200, 205); strokeWidth = 1f }

    /** @param client puede ser null si no se pudieron consultar las medidas. */
    suspend fun generate(project: Project, client: Client?, pieces: List<PatternPiece>): File =
        withContext(Dispatchers.IO) {
            val document = PdfDocument()
            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas = page.canvas
            var y = margin + 10f

            fun newPageIfNeeded(space: Float) {
                if (y + space > pageHeight - margin) {
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = page.canvas
                    y = margin + 10f
                }
            }

            fun section(title: String) {
                newPageIfNeeded(40f)
                y += 18f
                canvas.drawText(title, margin, y, sectionPaint)
                y += 6f
                canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
                y += 16f
            }

            fun row(label: String, value: String) {
                newPageIfNeeded(18f)
                canvas.drawText(label, margin, y, labelPaint)
                canvas.drawText(value, margin + 150f, y, valuePaint)
                y += 17f
            }

            // ---------- Encabezado ----------
            canvas.drawText("Albrimon", margin, y, titlePaint)
            y += 20f
            canvas.drawText("Ficha técnica de confección", margin, y, labelPaint)
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
            canvas.drawText("Generada: $today", pageWidth - margin - 110f, y, labelPaint)
            y += 8f

            // ---------- Proyecto ----------
            section("Proyecto")
            row("Prenda", project.name)
            row("Estado", project.status.label)
            row("Fecha de entrega", project.date)
            if (!project.notes.isNullOrBlank()) row("Notas", project.notes.take(70))

            // ---------- Cliente ----------
            section("Cliente")
            row("Nombre", project.clientName.ifBlank { client?.name ?: "—" })
            if (client != null) {
                row("Teléfono", client.phone.ifBlank { "—" })
                row("Talla", client.size.ifBlank { "—" })
                row("Edad", client.age?.let { "$it años" } ?: "—")
                section("Medidas corporales")
                val m = client.measurements
                row("Busto", cm(m.bust))
                row("Cintura", cm(m.waist))
                row("Cadera", cm(m.hip))
                row("Largo", cm(m.length))
            }

            // ---------- Piezas ----------
            section("Piezas de patronaje (${pieces.size})")
            if (pieces.isEmpty()) {
                canvas.drawText("Sin piezas registradas.", margin, y, labelPaint)
                y += 17f
            } else {
                newPageIfNeeded(18f)
                canvas.drawText("Pieza", margin, y, labelPaint)
                canvas.drawText("Medidas (cm)", margin + 220f, y, labelPaint)
                canvas.drawText("Girar", margin + 360f, y, labelPaint)
                y += 16f
                pieces.forEach { piece ->
                    newPageIfNeeded(18f)
                    canvas.drawText(piece.name.take(32), margin, y, valuePaint)
                    canvas.drawText("${mmToCm(piece.widthMm)} × ${mmToCm(piece.heightMm)}", margin + 220f, y, valuePaint)
                    canvas.drawText(if (piece.canRotate) "Sí" else "No", margin + 360f, y, valuePaint)
                    y += 17f
                }
            }

            document.finishPage(page)

            val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val safeName = project.name.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifBlank { "proyecto" }
            val file = File(dir, "ficha_${safeName}_${project.id}.pdf")
            try {
                file.outputStream().use { document.writeTo(it) }
            } finally {
                document.close()
            }
            file
        }

    private fun cm(value: Int) = if (value > 0) "$value cm" else "—"

    private fun mmToCm(mm: Int): String =
        if (mm % 10 == 0) "${mm / 10}" else String.format(Locale.US, "%.1f", mm / 10.0)
}
