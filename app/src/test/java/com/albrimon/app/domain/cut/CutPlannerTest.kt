package com.albrimon.app.domain.cut

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CutPlannerTest {

    private val scrapA = ScrapSlot(1, "A", 700, 400, hasGrain = false)
    private val scrapB = ScrapSlot(2, "B", 450, 350, hasGrain = false)

    @Test
    fun todasLasPiezasCabenYNoSeSalenDelRetazo() {
        val plan = CutPlanner.plan(
            listOf(scrapA, scrapB),
            listOf(
                Piece(1, "Frente", 350, 300, false),
                Piece(2, "Bolsillo", 180, 200, true),
                Piece(3, "Cintura", 400, 100, false)
            )
        )
        assertTrue(plan.unplaced.isEmpty())
        val slots = listOf(scrapA, scrapB).associateBy { it.id }
        plan.placements.forEach {
            val s = slots.getValue(it.scrapId)
            assertTrue("x+w fuera", it.x + it.w <= s.w)
            assertTrue("y+h fuera", it.y + it.h <= s.h)
        }
    }

    @Test
    fun lasPiezasNoSeEmpalman() {
        val plan = CutPlanner.plan(
            listOf(scrapA),
            List(6) { Piece(it + 1L, "P$it", 200, 150, true) }
        )
        val p = plan.placements
        for (i in p.indices) for (j in i + 1 until p.size) {
            val a = p[i]; val b = p[j]
            if (a.scrapId != b.scrapId) continue
            val overlap = a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h
            assertFalse("Empalme entre ${a.pieceId} y ${b.pieceId}", overlap)
        }
    }

    @Test
    fun conHiloNoSeGiraLaPieza() {
        val grain = ScrapSlot(1, "Estampado", 300, 600, hasGrain = true)
        // 500×200 solo cabe girada (200×500) en 300×600; con hilo debe quedar sin colocar.
        val plan = CutPlanner.plan(listOf(grain), listOf(Piece(1, "Larga", 500, 200, true)))
        assertEquals(1, plan.unplaced.size)
        assertTrue(plan.placements.isEmpty())
        assertFalse(plan.isComplete)
    }

    @Test
    fun sinHiloSiSeGiraSiLaPiezaLoPermite() {
        val plain = ScrapSlot(1, "Liso", 300, 600, hasGrain = false)
        val plan = CutPlanner.plan(listOf(plain), listOf(Piece(1, "Larga", 500, 200, true)))
        assertTrue(plan.isComplete)
        assertTrue(plan.placements.single().rotated)
    }

    @Test
    fun piezaMuyGrandeQuedaSinColocar() {
        val plan = CutPlanner.plan(listOf(scrapB), listOf(Piece(1, "Enorme", 900, 900, true)))
        assertEquals(1, plan.unplaced.size)
        assertTrue(plan.uses.isEmpty())
    }

    @Test
    fun usaPrimeroElRetazoMasPequenoDondeCabe() {
        val plan = CutPlanner.plan(listOf(scrapA, scrapB), listOf(Piece(1, "Chica", 200, 200, false)))
        assertEquals(2L, plan.placements.single().scrapId)
    }

    @Test
    fun sobranteYAreaSonCoherentesConLaValidacionDelBackend() {
        val plan = CutPlanner.plan(listOf(scrapA), listOf(Piece(1, "Tira", 700, 150, false)))
        val use = plan.uses.single()
        assertEquals(700L * 150, use.usedAreaMm2)
        assertEquals(700, use.leftoverWmm)
        assertEquals(250, use.leftoverHmm)
        // Misma regla que ScrapService.applyPlan: sobrante + usado <= área original
        assertTrue(use.leftoverWmm.toLong() * use.leftoverHmm + use.usedAreaMm2 <= 700L * 400)
    }

    @Test
    fun sobranteMenorAlMinimoNoSeRegistra() {
        val plan = CutPlanner.plan(listOf(scrapA), listOf(Piece(1, "Casi todo", 700, 350, false)))
        val use = plan.uses.single()
        assertEquals(0, use.leftoverWmm)
        assertEquals(0, use.leftoverHmm)
    }

    @Test
    fun piezasInvalidasNoRompenElPlan() {
        val plan = CutPlanner.plan(listOf(scrapA), listOf(Piece(1, "Cero", 0, 100, false)))
        assertEquals(1, plan.unplaced.size)
    }
}
