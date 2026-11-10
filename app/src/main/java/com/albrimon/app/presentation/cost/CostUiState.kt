package com.albrimon.app.presentation.cost

import com.albrimon.app.domain.cost.CostBreakdown
import com.albrimon.app.domain.cost.CostCalculator
import com.albrimon.app.domain.cost.MaterialLine

/** Un renglón de material tal como lo escribe el usuario (texto, para no perder lo que teclea). */
data class MaterialInput(
    val id: Int,
    val name: String = "",
    val unit: String = "",
    val quantity: String = "",
    val unitPrice: String = ""
) {
    fun toLine() = MaterialLine(CostCalculator.parse(quantity), CostCalculator.parse(unitPrice))
    val subtotal: Double get() = toLine().subtotal
}

data class CostUiState(
    val materials: List<MaterialInput> = listOf(MaterialInput(id = 1)),
    val labor: String = "",
    val contingencyPct: String = "5",
    val taxPct: String = "16"
) {
    /** El resumen se recalcula a partir de los campos; no se guarda por separado. */
    val breakdown: CostBreakdown
        get() = CostCalculator.calculate(
            materials = materials.map { it.toLine() },
            labor = CostCalculator.parse(labor),
            contingencyPct = CostCalculator.parse(contingencyPct),
            taxPct = CostCalculator.parse(taxPct)
        )
}
