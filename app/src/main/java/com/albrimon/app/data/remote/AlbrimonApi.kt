package com.albrimon.app.data.remote

import com.albrimon.app.data.remote.dto.ApplyPlanRequest
import com.albrimon.app.data.remote.dto.AuthResponse
import com.albrimon.app.data.remote.dto.ClientDto
import com.albrimon.app.data.remote.dto.ClientRequest
import com.albrimon.app.data.remote.dto.LoginRequest
import com.albrimon.app.data.remote.dto.PieceDto
import com.albrimon.app.data.remote.dto.PieceRequest
import com.albrimon.app.data.remote.dto.ProjectDto
import com.albrimon.app.data.remote.dto.ProjectRequest
import com.albrimon.app.data.remote.dto.RegisterRequest
import com.albrimon.app.data.remote.dto.ScrapDto
import com.albrimon.app.data.remote.dto.ScrapRequest
import com.albrimon.app.data.remote.dto.UsageDto
import com.albrimon.app.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Contrato REST del backend (Spring Boot). La URL base termina en /api/. */
interface AlbrimonApi {

    // ---------- Autenticación ----------
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): UserDto

    // ---------- Clientes ----------
    @GET("clients")
    suspend fun clients(): List<ClientDto>

    @GET("clients/{id}")
    suspend fun client(@Path("id") id: Long): ClientDto

    @GET("clients/me")
    suspend fun myClientProfile(): ClientDto

    @POST("clients")
    suspend fun createClient(@Body request: ClientRequest): ClientDto

    @PUT("clients/{id}")
    suspend fun updateClient(@Path("id") id: Long, @Body request: ClientRequest): ClientDto

    // ---------- Proyectos ----------
    @GET("projects")
    suspend fun projects(): List<ProjectDto>

    @GET("projects/{id}")
    suspend fun project(@Path("id") id: Long): ProjectDto

    @POST("projects")
    suspend fun createProject(@Body request: ProjectRequest): ProjectDto

    @PUT("projects/{id}")
    suspend fun updateProject(@Path("id") id: Long, @Body request: ProjectRequest): ProjectDto

    // ---------- Piezas de patronaje ----------
    @GET("projects/{projectId}/pieces")
    suspend fun pieces(@Path("projectId") projectId: Long): List<PieceDto>

    @POST("projects/{projectId}/pieces")
    suspend fun createPiece(@Path("projectId") projectId: Long, @Body request: PieceRequest): PieceDto

    @DELETE("projects/{projectId}/pieces/{pieceId}")
    suspend fun deletePiece(@Path("projectId") projectId: Long, @Path("pieceId") pieceId: Long)

    // ---------- Retazos ----------
    @GET("scraps")
    suspend fun scraps(): List<ScrapDto>

    @POST("scraps")
    suspend fun createScrap(@Body request: ScrapRequest): ScrapDto

    @PUT("scraps/{id}")
    suspend fun updateScrap(@Path("id") id: Long, @Body request: ScrapRequest): ScrapDto

    @DELETE("scraps/{id}")
    suspend fun deleteScrap(@Path("id") id: Long)

    @POST("scraps/apply-plan")
    suspend fun applyPlan(@Body request: ApplyPlanRequest): List<UsageDto>

    @GET("scraps/usage/project/{projectId}")
    suspend fun usage(@Path("projectId") projectId: Long): List<UsageDto>
}
