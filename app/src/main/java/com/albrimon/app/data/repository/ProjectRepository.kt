package com.albrimon.app.data.repository

import com.albrimon.app.data.remote.AlbrimonApi
import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.PieceRequest
import com.albrimon.app.data.remote.dto.ProjectRequest
import com.albrimon.app.data.remote.safeApiCall
import com.albrimon.app.domain.model.PatternPiece
import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus
import javax.inject.Inject
import javax.inject.Singleton

/** Proyectos y sus piezas de patronaje, desde la API REST (/api/projects). */
@Singleton
class ProjectRepository @Inject constructor(
    private val api: AlbrimonApi
) {
    suspend fun projects(): ApiResult<List<Project>> =
        safeApiCall { api.projects().map { it.toDomain() } }

    /** El backend no filtra por cliente, así que se filtra aquí. */
    suspend fun projectsByClient(clientId: Long): ApiResult<List<Project>> =
        when (val result = projects()) {
            is ApiResult.Success -> ApiResult.Success(result.data.filter { it.clientId == clientId })
            is ApiResult.Failure -> result
        }

    suspend fun project(id: Long): ApiResult<Project> =
        safeApiCall { api.project(id).toDomain() }

    suspend fun create(
        name: String,
        clientId: Long,
        dueDate: String?,
        notes: String?
    ): ApiResult<Project> = safeApiCall {
        api.createProject(
            ProjectRequest(
                name = name,
                clientId = clientId,
                status = ProjectStatus.PENDIENTE.name,
                dueDate = dueDate,
                notes = notes
            )
        ).toDomain()
    }

    /** Cambia el estado conservando los demás datos (el backend reemplaza todos los campos). */
    suspend fun updateStatus(project: Project, status: ProjectStatus): ApiResult<Project> = safeApiCall {
        api.updateProject(
            project.id,
            ProjectRequest(
                name = project.name,
                clientId = project.clientId,
                status = status.name,
                dueDate = project.dueDate,
                notes = project.notes
            )
        ).toDomain()
    }

    suspend fun pieces(projectId: Long): ApiResult<List<PatternPiece>> =
        safeApiCall { api.pieces(projectId).map { it.toDomain() } }

    suspend fun addPiece(projectId: Long, request: PieceRequest): ApiResult<PatternPiece> =
        safeApiCall { api.createPiece(projectId, request).toDomain() }

    suspend fun deletePiece(projectId: Long, pieceId: Long): ApiResult<Unit> =
        safeApiCall { api.deletePiece(projectId, pieceId) }
}
