package com.albrimon.app.domain.cut

/*
 * Optimizador de cortes por estantes (shelf packing). Kotlin puro, sin dependencias de Android,
 * para poder probarlo con JUnit. Todas las medidas en milímetros.
 *
 * Regla: las piezas se ordenan de la más grande a la más chica y se colocan en filas (estantes)
 * dentro del retazo. Se prueba primero el retazo más pequeño donde quepa, para conservar los grandes.
 * Una pieza solo se gira 90° si la pieza lo permite Y el retazo no tiene hilo/estampado direccional.
 */

data class Piece(val id: Long, val name: String, val w: Int, val h: Int, val canRotate: Boolean)

data class ScrapSlot(val id: Long, val name: String, val w: Int, val h: Int, val hasGrain: Boolean)

data class Placement(
    val pieceId: Long,
    val scrapId: Long,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    val rotated: Boolean
)

/** Resultado por retazo usado; es lo que se envía a POST /api/scraps/apply-plan. */
data class ScrapUse(
    val scrapId: Long,
    val usedAreaMm2: Long,
    val leftoverWmm: Int,
    val leftoverHmm: Int,
    val usagePct: Double
)

data class CutPlan(
    val placements: List<Placement>,
    val unplaced: List<Piece>,
    val uses: List<ScrapUse>
) {
    val isComplete: Boolean get() = unplaced.isEmpty() && placements.isNotEmpty()
}

object CutPlanner {

    /** Un sobrante menor a esto (en ambas medidas) no se registra como retazo útil. */
    const val MIN_LEFTOVER_MM = 100

    private class Shelf(val y: Int, val height: Int, var usedW: Int)

    private class Bin(val slot: ScrapSlot) {
        val shelves = mutableListOf<Shelf>()
        var usedH = 0

        /** Devuelve (x, y) o null si la pieza w×h no cabe. */
        fun tryPlace(pw: Int, ph: Int): Pair<Int, Int>? {
            if (pw > slot.w || ph > slot.h) return null
            shelves.firstOrNull { ph <= it.height && it.usedW + pw <= slot.w }?.let { shelf ->
                val x = shelf.usedW
                shelf.usedW += pw
                return x to shelf.y
            }
            if (usedH + ph > slot.h) return null
            val shelf = Shelf(usedH, ph, pw)
            shelves += shelf
            usedH += ph
            return 0 to shelf.y
        }
    }

    fun plan(
        scraps: List<ScrapSlot>,
        pieces: List<Piece>,
        minLeftoverMm: Int = MIN_LEFTOVER_MM
    ): CutPlan {
        val bins = scraps
            .filter { it.w > 0 && it.h > 0 }
            .sortedBy { it.w.toLong() * it.h }
            .map { Bin(it) }
        val placements = mutableListOf<Placement>()
        val unplaced = mutableListOf<Piece>()

        val ordered = pieces.sortedByDescending { maxOf(it.w, it.h) }
        for (piece in ordered) {
            if (piece.w <= 0 || piece.h <= 0) {
                unplaced += piece
                continue
            }
            var done = false
            loop@ for (bin in bins) {
                val orientations = buildList {
                    add(Triple(piece.w, piece.h, false))
                    if (piece.canRotate && !bin.slot.hasGrain && piece.w != piece.h) {
                        add(Triple(piece.h, piece.w, true))
                    }
                }
                for ((w, h, rotated) in orientations) {
                    val pos = bin.tryPlace(w, h) ?: continue
                    placements += Placement(piece.id, bin.slot.id, pos.first, pos.second, w, h, rotated)
                    done = true
                    break@loop
                }
            }
            if (!done) unplaced += piece
        }

        val uses = bins.mapNotNull { bin ->
            val mine = placements.filter { it.scrapId == bin.slot.id }
            if (mine.isEmpty()) return@mapNotNull null
            val used = mine.sumOf { it.w.toLong() * it.h }
            val total = bin.slot.w.toLong() * bin.slot.h
            // Sobrante reutilizable: la franja completa que queda debajo del último estante.
            val freeH = bin.slot.h - bin.usedH
            val hasLeftover = freeH >= minLeftoverMm && bin.slot.w >= minLeftoverMm
            ScrapUse(
                scrapId = bin.slot.id,
                usedAreaMm2 = used,
                leftoverWmm = if (hasLeftover) bin.slot.w else 0,
                leftoverHmm = if (hasLeftover) freeH else 0,
                usagePct = 100.0 * used / total
            )
        }
        return CutPlan(placements, unplaced, uses)
    }
}
