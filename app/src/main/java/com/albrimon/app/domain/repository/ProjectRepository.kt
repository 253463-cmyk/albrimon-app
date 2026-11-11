package com.albrimon.app.domain.repository

import com.albrimon.app.data.remote.ApiResult
import com.albrimon.app.data.remote.dto.PieceRequest
import com.albrimon.app.domain.model.PatternPiece
import com.albrimon.app.domain.model.Project
import com.albrimon.app.domain.model.ProjectStatus

/** Contrato de proyectos y sus piezas de patronaje. */
interface ProjectRepository {
    suspend fun projects(): ApiResult<List<Project>>

    suspend fun projectsByClient(clientId: Long): ApiResult<List<Project>>

    suspend fun project(id: Long): ApiResult<Project>

    suspend fun create(name: String, clientId: Long, dueDate: String?, notes: String?): ApiResult<Project>

    suspend fun updateStatus(project: Project, status: ProjectStatus): ApiResult<Project>

    suspend fun pieces(projectId: Long): ApiResult<List<PatternPiece>>

    suspend fun addPiece(projectId: Long, request: PieceRequest): ApiResult<PatternPiece>

    suspend fun deletePiece(projectId: Long, pieceId: Long): ApiResult<Unit>
}
