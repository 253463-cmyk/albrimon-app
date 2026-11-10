package com.albrimon.app.domain.model

/** Retazo del inventario. Medidas en milímetros enteros para evitar errores de decimales. */
data class Scrap(
    val id: Long,
    val name: String,
    val fabricType: String,
    val colorHex: String,
    val widthMm: Int,
    val heightMm: Int,
    val hasGrain: Boolean,
    val photoPath: String?,
    val available: Boolean
) {
    val areaMm2: Long get() = widthMm.toLong() * heightMm
}

data class ScrapUsage(
    val id: Long,
    val projectId: Long,
    val scrapId: Long,
    val scrapName: String,
    val usedAreaMm2: Long,
    val leftoverWmm: Int,
    val leftoverHmm: Int,
    val createdAt: String?
)
