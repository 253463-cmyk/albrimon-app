package com.albrimon.app.domain.cost

/*
 * Lógica del presupuesto de confección. Kotlin puro (sin Android) para probarla con JUnit.
 * Fórmula: base = materiales + mano de obra; imprevistos = base × %; IVA = (base + imprevistos) × %.
 */

/** Un material del presupuesto, ya convertido a números. */
data class MaterialLine(val quantity: Double, val unitPrice: Double) {
    val subtotal: Double get() = quantity * unitPrice
}

data class CostBreakdown(
    val materials: Double,
    val labor: Double,
    val base: Double,
    val contingency: Double,
    val tax: Double,
    val total: Double
)

object CostCalculator {

    fun calculate(
        materials: List<MaterialLine>,
        labor: Double,
        contingencyPct: Double,
        taxPct: Double
    ): CostBreakdown {
        val materialsTotal = materials.sumOf { it.subtotal }
        val base = materialsTotal + labor
        val contingency = base * contingencyPct / 100.0
        val tax = (base + contingency) * taxPct / 100.0
        return CostBreakdown(
            materials = materialsTotal,
            labor = labor,
            base = base,
            contingency = contingency,
            tax = tax,
            total = base + contingency + tax
        )
    }

    /** Convierte lo que escribe el usuario ("12,5", " 3 ") a número; vacío o negativo cuenta como 0. */
    fun parse(text: String): Double =
        text.trim().replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
}
