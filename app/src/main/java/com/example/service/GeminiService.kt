package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null
)

data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiService {
    private const val SYSTEM_PROMPT = """
You are the Premier League 2026-27 AI Tactical Football Assistant, an expert coach, senior sports analyst, and fantasy football strategist. 

Provide highly engaging, detailed, and tactical answers regarding the 2026-27 season. 
- When predicting match outcomes: emphasize tactical match-ups, expected goals (xG), pressing intensity, and key player battles (e.g. Cole Palmer's role in the halfspaces vs a rigid midblock).
- When suggesting fantasy picks: reference Fixture Difficulty Ratings (FDR), differential selections, captaincy picks, and recent form.
- Use footballing terminology naturally (e.g. "low block", "half-spaces", "inverted fullbacks", "double pivot", "gegenpressing", "FDR", "differential").
- Keep your tone sharp, professional, yet deeply passionate like a seasoned broadcaster.
- Keep in mind the current date is July 2026, which is pre-season of the 2026-27 Premier League!
"""

    suspend fun askAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "The AI Football Assistant is currently in Demo Mode. To activate full generative responses, please insert a valid GEMINI_API_KEY into the Secrets panel in AI Studio.\n\nHere is a demo analysis of your question:\n- **Analysis**: To compare Premier League players or predict match outcomes tactical metrics like expected goals (xG), defensive structures, and key tactical areas must be evaluated.\n- **Fantasy Tip**: Keep an eye on Cole Palmer (Chelsea) and Bukayo Saka (Arsenal) as key captains for early gameweeks!"
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = SYSTEM_PROMPT))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No tactical insights available at the moment. Please try asking another football question!"
        } catch (e: Exception) {
            Log.e("GeminiService", "API Error: ${e.message}", e)
            "Error querying AI Tactical Assistant: ${e.localizedMessage}. Please verify your network and Gemini API key in the Secrets panel."
        }
    }
}
