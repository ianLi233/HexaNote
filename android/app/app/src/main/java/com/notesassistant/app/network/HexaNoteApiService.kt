package com.notesassistant.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface HexaNoteApiService {
    
    @POST("token")
    suspend fun getToken(
        @Body request: TokenRequest
    ): Response<TokenResponse>

    @POST("chat/query")
    suspend fun chatQuery(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
    
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
    
    @POST("chat/sessions")
    suspend fun createSession(
        @Header("Authorization") token: String
    ): Response<SessionCreateResponse>

    @GET("notes/search/semantic")
    suspend fun searchNotes(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("tags") tags: String? = null
    ): Response<SemanticSearchResponse>

    @GET("notes/{note_id}/search")
    suspend fun searchWithinNote(
        @Header("Authorization") token: String,
        @Path("note_id") noteId: String,
        @Query("q") query: String,
        @Query("window") window: Int = 2
    ): Response<NoteSearchResponse>

    @POST("notes/reindex")
    suspend fun reindexNotes(
        @Header("Authorization") token: String
    ): Response<ReindexResponse>
}

data class TokenRequest(
    @SerializedName("password") val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class ChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("limit") val limit: Int = 5,
    @SerializedName("additional_context") val additionalContext: String? = null
)

data class ChatResponse(
    @SerializedName("message") val message: String,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("context_notes") val contextNotes: List<ContextNote>? = null,
    @SerializedName("created_at") val createdAt: String
)

data class ContextNote(
    @SerializedName("note_id") val noteId: String,
    @SerializedName("title") val title: String,
    @SerializedName("content_preview") val contentPreview: String,
    @SerializedName("relevance_score") val relevanceScore: Double?
)

data class SemanticSearchResponse(
    @SerializedName("results") val results: List<SemanticSearchResult>,
    @SerializedName("query") val query: String,
    @SerializedName("count") val count: Int
)

data class SemanticSearchResult(
    @SerializedName("note_id") val noteId: String,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String,
    @SerializedName("relevance_score") val relevanceScore: Double?,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class NoteSearchResponse(
    @SerializedName("context") val context: String,
    @SerializedName("title") val title: String,
    @SerializedName("chunk_range") val chunkRange: String,
    @SerializedName("total_chunks") val totalChunks: Int,
    @SerializedName("best_chunk_index") val bestChunkIndex: Int
)

data class ReindexResponse(
    @SerializedName("message") val message: String,
    @SerializedName("total") val total: Int,
    @SerializedName("success") val success: Int,
    @SerializedName("errors") val errors: Int
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("version") val version: String
)

data class SessionCreateResponse(
    @SerializedName("session_id") val sessionId: String
)
