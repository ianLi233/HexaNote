package com.notesassistant.app.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("upload-note")
    suspend fun uploadNote(
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>
    
    @POST("query")
    suspend fun queryNotes(
        @Body request: QueryRequest
    ): Response<QueryResponse>
}

data class UploadResponse(
    val noteId: String,
    val status: String,
    val message: String? = null
)

data class QueryRequest(
    val query: String,
    val noteId: String? = null
)

data class QueryResponse(
    val answer: String,
    val status: String? = null
)
