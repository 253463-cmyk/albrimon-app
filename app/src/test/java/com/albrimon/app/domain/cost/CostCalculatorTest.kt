package com.albrimon.app.domain.cost

import org.junit.Assert.assertEquals
import org.junit.Test

class CostCalculatorTest {

    private val delta = 0.001

    @Test
    fun calculaSubtotalesBaseImprevistosEIva() {
        // Tela: 2 m × $150 = 300; botones: 6 × $5 = 30 -> materiales 330
        val result = CostCalculator.calculate(
            materials = listOf(MaterialLine(2.0, 150.0), MaterialLine(6.0, 5.0)),
            labor = 400.0,
            contingencyPct = 5.0,
            taxPct = 16.0
        )
        assertEquals(330.0, result.materials, delta)
        assertEquals(730.0, result.base, delta)
        assertEquals(36.5, result.contingency, delta)       // 730 × 5 %
        assertEquals(122.64, result.tax, delta)             // (730 + 36.5) × 16 %
        assertEquals(889.14, result.total, delta)
    }

    @Test
    fun sinMaterialesNiPorcentajesElTotalEsLaManoDeObra() {
        val result = CostCalculator.calculate(emptyList(), labor = 250.0, contingencyPct = 0.0, taxPct = 0.0)
        assertEquals(250.0, result.total, delta)
    }

    @Test
    fun parseAceptaComaDecimalYTrataTextoInvalidoComoCero() {
        assertEquals(12.5, CostCalculator.parse("12,5"), delta)
        assertEquals(3.0, CostCalculator.parse(" 3 "), delta)
        assertEquals(0.0, CostCalculator.parse("abc"), delta)
        assertEquals(0.0, CostCalculator.parse("-8"), delta)
        assertEquals(0.0, CostCalculator.parse(""), delta)
    }
}
