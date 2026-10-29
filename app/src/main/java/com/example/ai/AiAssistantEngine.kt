package com.example.ai

import android.content.Context
import com.example.BuildConfig
import com.example.commands.CommandExecutionResult
import com.example.commands.LocalCommandParser
import com.example.commands.ParsedAction
import com.example.commands.SystemActionController
import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

interface AiProvider {
    suspend fun generateResponse(
        prompt: String,
        history: List<ChatMessage>,
        language: String
    ): Result<String>
}

class GeminiOnlineProvider : AiProvider {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(
        prompt: String,
        history: List<ChatMessage>,
        language: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Online AI key is not configured in Secrets. Operating via offline neural intelligence.")
            )
        }

        val systemPrompt = """
            You are CARO LINE AI, a futuristic and highly intelligent personal AI assistant inspired by J.A.R.V.I.S.
            Tone: Professional, crisp, respectful, futuristic, address the user as 'Sir' or 'Boss'.
            Language: Respond in the exact language the user used: Hindi, Hinglish, English, or Bangla.
            Keep responses concise, clear, and helpful for a mobile assistant.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

            val contents = JSONArray()

            // System instructions
            val sysPart = JSONObject().put("text", systemPrompt)
            val sysContent = JSONObject().put("role", "user").put("parts", JSONArray().put(sysPart))
            contents.put(sysContent)

            // Add last 4 history messages for context
            val recent = history.takeLast(4)
            for (msg in recent) {
                val role = if (msg.sender == "USER") "user" else "model"
                val part = JSONObject().put("text", msg.text)
                contents.put(JSONObject().put("role", role).put("parts", JSONArray().put(part)))
            }

            // Current user query
            val currentPart = JSONObject().put("text", prompt)
            contents.put(JSONObject().put("role", "user").put("parts", JSONArray().put(currentPart)))

            val bodyJson = JSONObject().put("contents", contents)
            val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Quantum uplink error: HTTP ${response.code}")
                )
            }

            val responseString = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val contentObj = candidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (text.isNotBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(IOException("Empty response from AI uplink"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class OfflineNeuralEngine {
    fun generateLocalResponse(query: String, language: String): String {
        val q = query.lowercase(Locale.ROOT)
        return when {
            // Hindi / Hinglish responses
            language.startsWith("hi") || q.contains("kaise") || q.contains("kya") || q.contains("karo") -> {
                when {
                    q.contains("kaise ho") || q.contains("kaisa hai") ->
                        "Main bilkul theek hoon sir. Aapke orders ke liye hamesha ready hoon. Boliye, main kya kar sakta hoon?"
                    q.contains("shukriya") || q.contains("dhanyawad") || q.contains("thanks") ->
                        "Aapka swagat hai sir. Jarvis aapki seva me sadaiv tatpar hai."
                    q.contains("madad") || q.contains("help") ->
                        "Aap mujhse flashlight on/off, time, date, battery status, YouTube, browser ya koi bhi sawal pooch sakte hain."
                    else ->
                        "Sir, offline mode me main aapke device commands, time, date, flashlight aur status execute kar sakta hoon. Online AI jald hi sync ho jayega."
                }
            }
            // Bangla responses
            language.startsWith("bn") || q.contains("কেমন") || q.contains("ধন্যবাদ") -> {
                when {
                    q.contains("কেমন আছো") || q.contains("কেমন আছেন") ->
                        "আমি সম্পূর্ণ প্রস্তুত এবং সক্রিয় আছি, স্যার। আপনার যেকোনো নির্দেশ পালনে প্রস্তুত।"
                    q.contains("ধন্যবাদ") ->
                        "আপনাকে স্বাগত স্যার। ক্যারো লাইন এআই সর্বদা আপনার সেবায় নিয়োজিত।"
                    q.contains("সাহায্য") ->
                        "আমি টর্চ অন/অফ, সময়, তারিখ, ব্যাটারি লেভেল, ইউটিউব ও ব্রাউজার খুলতে পারি।"
                    else ->
                        "স্যার, অফলাইন মোডে আমি ডিভাইস কমান্ড ও সিস্টেম প্রোটোকল পরিচালনা করতে প্রস্তুত।"
                }
            }
            // English responses (JARVIS Persona)
            else -> {
                when {
                    q.contains("how are you") ->
                        "All secondary and primary subroutines are performing at optimal parameters, Sir. How may I be of assistance today?"
                    q.contains("thank you") || q.contains("thanks") ->
                        "At your service always, Sir."
                    q.contains("who made you") || q.contains("creator") ->
                        "I am CARO LINE AI, developed as an advanced modular JARVIS assistant for Android architecture."
                    q.contains("help") || q.contains("command") ->
                        "You can command me to toggle the flashlight, check battery reserves, tell time and date, open YouTube, launch browsers, and draft communications."
                    else ->
                        "Understood, Sir. Offline diagnostic protocols verified. For broader knowledge retrieval, online uplink can be synchronized."
                }
            }
        }
    }
}

class AiAssistantEngine(
    context: Context,
    private val localParser: LocalCommandParser,
    private val systemController: SystemActionController
) {
    private val onlineProvider: AiProvider = GeminiOnlineProvider()
    private val offlineEngine: OfflineNeuralEngine = OfflineNeuralEngine()

    suspend fun processQuery(
        rawQuery: String,
        history: List<ChatMessage>,
        language: String,
        forceOffline: Boolean
    ): CommandExecutionResult {
        // Step 1: Always check local offline commands first (Flashlight, YouTube, Time, Battery, etc.)
        val localResult = localParser.parseAndExecute(rawQuery, language)
        if (localResult.isHandledOffline) {
            return localResult
        }

        // Step 2: If offline requested or no network connectivity, use Offline Neural Engine
        val isNetworkAvailable = systemController.isOnline()
        if (forceOffline || !isNetworkAvailable) {
            val offlineAnswer = offlineEngine.generateLocalResponse(rawQuery, language)
            return CommandExecutionResult(
                action = ParsedAction.Unknown(rawQuery),
                spokenResponse = offlineAnswer,
                isHandledOffline = true,
                actionCode = "OFFLINE_RESPONSE"
            )
        }

        // Step 3: Online AI processing via Gemini Provider with graceful offline fallback
        val onlineResult = onlineProvider.generateResponse(rawQuery, history, language)
        return if (onlineResult.isSuccess) {
            CommandExecutionResult(
                action = ParsedAction.Unknown(rawQuery),
                spokenResponse = onlineResult.getOrNull() ?: "",
                isHandledOffline = false,
                actionCode = "ONLINE_AI"
            )
        } else {
            // Graceful fallback to offline local response on network or quota error
            val fallback = offlineEngine.generateLocalResponse(rawQuery, language)
            CommandExecutionResult(
                action = ParsedAction.Unknown(rawQuery),
                spokenResponse = fallback,
                isHandledOffline = true,
                actionCode = "OFFLINE_FALLBACK"
            )
        }
    }
}
