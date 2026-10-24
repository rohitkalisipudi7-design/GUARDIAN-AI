package com.example.guardianai.pipeline

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Direct Gemini API Client & Dynamic RAG Orchestrator
 * Integrates Google Gemini Generative AI REST API with local RAG context injection.
 */
object GeminiApiClient {

    private const val GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    private var apiKey: String = "" // Optional user/system API key

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    private val gson = Gson()

    fun setApiKey(key: String) {
        apiKey = key.trim()
    }

    fun hasApiKey(): Boolean = apiKey.isNotBlank()

    /**
     * Executes a dynamic RAG query via Gemini API if key is available, returning null if API key missing or offline.
     */
    suspend fun generateWithGeminiAPI(userMessage: String, contextBlock: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null

        try {
            val fullPrompt = GuardianSystemPrompt.buildRAGPromptPayload(userMessage, contextBlock)
            val jsonBody = JsonObject().apply {
                val contents = com.google.gson.JsonArray().apply {
                    val contentObj = JsonObject().apply {
                        val parts = com.google.gson.JsonArray().apply {
                            val partObj = JsonObject().apply {
                                addProperty("text", fullPrompt)
                            }
                            add(partObj)
                        }
                        add("parts", parts)
                    }
                    add(contentObj)
                }
                add("contents", contents)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "$GEMINI_ENDPOINT?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: return@withContext "Gemini API Error: Empty response body"
                val rootJson = gson.fromJson(respStr, JsonObject::class.java)
                val candidates = rootJson.getAsJsonArray("candidates")
                if (candidates != null && candidates.size() > 0) {
                    val firstCand = candidates[0].asJsonObject
                    val content = firstCand.getAsJsonObject("content")
                    val parts = content.getAsJsonArray("parts")
                    if (parts != null && parts.size() > 0) {
                        return@withContext parts[0].asJsonObject.get("text").asString
                    }
                }
                return@withContext "Gemini API Error: Unexpected JSON structure: $respStr"
            } else {
                if (response.code == 404) {
                    try {
                        val listReq = okhttp3.Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
                            .get()
                            .build()
                        val listResp = httpClient.newCall(listReq).execute()
                        val listStr = listResp.body?.string() ?: ""
                        
                        // Parse JSON to only show model names to keep it short
                        val root = gson.fromJson(listStr, JsonObject::class.java)
                        val modelsArr = root.getAsJsonArray("models")
                        val names = mutableListOf<String>()
                        if (modelsArr != null) {
                            for (i in 0 until modelsArr.size()) {
                                names.add(modelsArr[i].asJsonObject.get("name").asString)
                            }
                        }
                        return@withContext "Gemini API HTTP Error 404. Available models for this key: ${names.joinToString(", ")}"
                    } catch (e: Exception) {
                        return@withContext "Gemini API HTTP Error: 404. Failed to list models: ${e.message}"
                    }
                }
                return@withContext "Gemini API HTTP Error: ${response.code} - ${response.body?.string()}"
            }
        } catch (e: Exception) {
            return@withContext "Gemini API Exception: ${e.message}"
        }
    }
}
