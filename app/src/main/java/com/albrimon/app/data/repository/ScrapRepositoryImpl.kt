package com.albrimon.app.data.repository

import com.albrimon.app.data.local.PhotoStorage
import com.albrimon.app.data.local.dao.ScrapDao
import com.albrimon.app.data.local.dao.UsageDao
import com.albrimon.app.domain.model.Scrap
import com.albrimon.app.domain.model.ScrapUsage
import com.albrimon.app.data.remote.AlbrimonApi
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.ApplyPlanRequest
import com.albrimon.app.data.remote.dto.PieceDto
import com.albrimon.app.data.remote.dto.ProjectDto
import com.albrimon.app.data.remote.dto.ScrapRequest
import com.albrimon.app.data.remote.dto.UsageInput
import com.albrimon.app.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.albrimon.app.domain.repository.ScrapRepository

/**
 * Único punto de acceso a retazos para los ViewModels.
 * API (Spring Boot) = fuente de verdad; Room = caché que permite ver el inventario sin conexión.
 */
@Singleton
class ScrapRepositoryImpl @Inject constructor(
    private val api: AlbrimonApi,
    private val scrapDao: ScrapDao,
    private val usageDao: UsageDao,
    private val photos: PhotoStorage
) : ScrapRepository {
    override val scraps: Flow<List<Scrap>> = scrapDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getCached(id: Long): Scrap? = scrapDao.get(id)?.toDomain()

    /** Descarga el inventario y reemplaza la caché local. */
    override suspend fun refresh(): ApiResult<Unit> =
        when (val result = safeApiCall { api.scraps() }) {
            is ApiResult.Success -> {
                scrapDao.replaceAll(result.data.map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Failure -> result
        }

    /** Crea (id == null) o actualiza un retazo. Solo cachea localmente si la API confirmó. */
    override suspend fun save(id: Long?, request: ScrapRequest): ApiResult<Scrap> =
        when (val result = safeApiCall {
            if (id == null) api.createScrap(request) else api.updateScrap(id, request)
        }) {
            is ApiResult.Success -> {
                val entity = result.data.toEntity()
                scrapDao.upsert(entity)
                ApiResult.Success(entity.toDomain())
            }
            is ApiResult.Failure -> result
        }

    override suspend fun delete(scrap: Scrap): ApiResult<Unit> =
        when (val result = safeApiCall { api.deleteScrap(scrap.id) }) {
            is ApiResult.Success -> {
                scrapDao.delete(scrap.id)
                photos.delete(scrap.photoPath)
                ApiResult.Success(Unit)
            }
            is ApiResult.Failure -> result
        }

    override suspend fun projects(): ApiResult<List<ProjectDto>> = safeApiCall { api.projects() }

    override suspend fun pieces(projectId: Long): ApiResult<List<PieceDto>> = safeApiCall { api.pieces(projectId) }

    override fun usage(projectId: Long): Flow<List<ScrapUsage>> =
        usageDao.observeByProject(projectId).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshUsage(projectId: Long): ApiResult<Unit> =
        when (val result = safeApiCall { api.usage(projectId) }) {
            is ApiResult.Success -> {
                usageDao.replaceForProject(projectId, result.data.map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Failure -> result
        }

    /**
     * Envía a la API el plan calculado por CutPlanner. El backend valida y guarda en una
     * transacción; solo si responde OK se refresca inventario e historial.
     */
    override suspend fun applyPlan(projectId: Long, usages: List<UsageInput>): ApiResult<Int> =
        when (val result = safeApiCall { api.applyPlan(ApplyPlanRequest(projectId, usages)) }) {
            is ApiResult.Success -> {
                refresh()
                refreshUsage(projectId)
                ApiResult.Success(result.data.size)
            }
            is ApiResult.Failure -> result
        }
}
