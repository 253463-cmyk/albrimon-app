package com.albrimon.app.data.repository

import com.albrimon.app.data.remote.dto.ClientDto
import com.albrimon.app.data.remote.dto.PieceDto
import com.albrimon.app.data.remote.dto.ProjectDto
import com.albrimon.app.domain.model.Client
import com.albrimon.app.domain.model.Measurements
import com.albrimon.app.domain.model.PatternPiece
import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus

// Conversión de los DTO de la API a los modelos que usan las pantallas.

fun ClientDto.toDomain() = Client(
    id = id,
    name = name,
    size = size.orEmpty(),
    age = age,
    isActive = active,
    phone = phone.orEmpty(),
    measurements = Measurements(
        bust = bustCm ?: 0,
        waist = waistCm ?: 0,
        hip = hipCm ?: 0,
        length = lengthCm ?: 0
    ),
    linkCode = linkCode
)

fun ProjectDto.toDomain() = Project(
    id = id,
    clientId = clientId,
    name = name,
    clientName = clientName.orEmpty(),
    status = ProjectStatus.fromApi(status),
    date = formatDueDate(dueDate),
    dueDate = dueDate,
    notes = notes
)

fun PieceDto.toDomain() = PatternPiece(id, name, widthMm, heightMm, canRotate)

private val MONTHS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

/**
 * "2026-12-12" -> "12 de diciembre de 2026". Se hace a mano (sin java.time)
 * porque la app soporta Android 7 (minSdk 24).
 */
fun formatDueDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "Sin fecha de entrega"
    val parts = iso.take(10).split("-")
    val year = parts.getOrNull(0)
    val month = parts.getOrNull(1)?.toIntOrNull()
    val day = parts.getOrNull(2)?.toIntOrNull()
    if (year == null || month == null || day == null || month !in 1..12) return iso
    return "$day de ${MONTHS[month - 1]} de $year"
}
