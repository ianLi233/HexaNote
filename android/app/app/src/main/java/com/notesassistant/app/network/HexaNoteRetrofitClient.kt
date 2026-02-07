package com.notesassistant.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object HexaNoteRetrofitClient {
    // Tailscale IP of your server machine
    // Base URL must end with / for Retrofit
    private const val BASE_URL = "http://100.103.146.10:8001/api/v1/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Set long timeouts for RAG processing
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        // Add trailing slash behavior if needed by server redirects
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: HexaNoteApiService = retrofit.create(HexaNoteApiService::class.java)
}
