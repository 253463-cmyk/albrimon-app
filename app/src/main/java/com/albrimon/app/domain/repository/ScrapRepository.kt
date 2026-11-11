package com.albrimon.app.domain.repository

import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.PieceDto
import com.albrimon.app.data.remote.dto.ProjectDto
import com.albrimon.app.data.remote.dto.ScrapRequest
import com.albrimon.app.data.remote.dto.UsageInput
import com.albrimon.app.domain.model.Scrap
import com.albrimon.app.domain.model.ScrapUsage
import kotlinx.coroutines.flow.Flow

/** Contrato de retazos: API como fuente de verdad y Room como caché sin conexión. */
interface ScrapRepository {
    val scraps: Flow<List<Scrap>>

    suspend fun getCached(id: Long): Scrap?

    /** Descarga el inventario y reemplaza la caché local. */
    suspend fun refresh(): ApiResult<Unit>

    /** Crea (id == null) o actualiza un retazo. */
    suspend fun save(id: Long?, request: ScrapRequest): ApiResult<Scrap>

    suspend fun delete(scrap: Scrap): ApiResult<Unit>

    suspend fun projects(): ApiResult<List<ProjectDto>>

    suspend fun pieces(projectId: Long): ApiResult<List<PieceDto>>

    fun usage(projectId: Long): Flow<List<ScrapUsage>>

    suspend fun refreshUsage(projectId: Long): ApiResult<Unit>

    /** Envía a la API el plan calculado por CutPlanner y refresca inventario e historial. */
    suspend fun applyPlan(projectId: Long, usages: List<UsageInput>): ApiResult<Int>
}
