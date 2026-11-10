package com.albrimon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Copia local (Room) del inventario de retazos. El id es el del servidor,
 * así la pantalla funciona sin conexión y se sincroniza al refrescar.
 * photoPath es relativo a filesDir (ej. "scrap_photos/abc.jpg"), nunca una ruta absoluta.
 */
@Entity(tableName = "scraps")
data class ScrapEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val fabricType: String,
    val colorHex: String,
    val widthMm: Int,
    val heightMm: Int,
    val hasGrain: Boolean,
    val photoPath: String?,
    val available: Boolean
)

/** Historial de uso de retazos por proyecto. */
@Entity(tableName = "scrap_usage")
data class UsageEntity(
    @PrimaryKey val id: Long,
    val projectId: Long,
    val scrapId: Long,
    val scrapName: String,
    val usedAreaMm2: Long,
    val leftoverWmm: Int,
    val leftoverHmm: Int,
    val createdAt: String?
)
