package com.albrimon.app.domain.model

/** Medidas corporales en centímetros. 0 significa "sin registrar". */
data class Measurements(
    val bust: Int,
    val waist: Int,
    val hip: Int,
    val length: Int
)

data class Client(
    val id: Long,
    val name: String,
    val size: String,
    /** null si no se registró la edad. */
    val age: Int?,
    val isActive: Boolean,
    val phone: String,
    val measurements: Measurements,
    /** Código para que la clienta vincule su cuenta (solo lo ve el diseñador). */
    val linkCode: String? = null
) {
    /** Texto corto para tarjetas: "Talla M · 28 años". */
    val summary: String
        get() = listOfNotNull(
            size.takeIf { it.isNotBlank() }?.let { "Talla $it" },
            age?.let { "$it años" }
        ).joinToString("  ·  ").ifBlank { "Sin talla ni edad registradas" }
}
