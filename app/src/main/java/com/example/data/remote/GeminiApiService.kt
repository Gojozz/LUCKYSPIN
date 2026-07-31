package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(val text: String? = null)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

data class GeminiCandidate(val content: GeminiContent?)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun getAiSupportReply(userQuery: String, chatHistory: List<Pair<String, String>>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return fallbackAiReply(userQuery)
        }

        val systemPrompt = "You are 'SpinBot', the friendly 24/7 AI Customer Support agent for Lucky Spin Multiplayer. " +
                "You answer queries regarding: 1-25 lucky spin number betting, room timers (120s), 20% system fee, " +
                "Bonus Coins vs Real Coins, Room Jackpots vs Global Jackpot rollover, deposit & withdrawal guidelines. " +
                "Be polite, enthusiastic, concise, and helpful. If a user asks to open a support ticket or report a complex bug, " +
                "instruct them to tap 'Create Support Ticket'."

        val historyParts = mutableListOf<GeminiContent>()
        for ((sender, text) in chatHistory.takeLast(10)) {
            val role = if (sender == "user") "user" else "model"
            historyParts.add(GeminiContent(parts = listOf(GeminiPart(text = "$role: $text"))))
        }
        historyParts.add(GeminiContent(parts = listOf(GeminiPart(text = "user: $userQuery"))))

        val request = GeminiRequest(
            contents = historyParts,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            replyText ?: fallbackAiReply(userQuery)
        } catch (e: Exception) {
            fallbackAiReply(userQuery)
        }
    }

    private fun fallbackAiReply(query: String): String {
        val lower = query.lowercase()
        return when {
            "fee" in lower || "system" in lower ->
                "Lucky Spin takes a default 20% system fee from the total winning pot. If no one wins a round, 100% of the pot moves into the Room Jackpot with 0% fee!"
            "coin" in lower || "bonus" in lower || "real" in lower ->
                "We offer two wallet types: Bonus Coins (granted upon signup, playable only) and Real Coins (won from games or deposited, playable and withdrawable)."
            "jackpot" in lower ->
                "Each room has a Room Jackpot. If a room stays inactive, its jackpot rolls over into the Global Jackpot, which is distributed in special events!"
            "ticket" in lower || "help" in lower || "agent" in lower || "human" in lower ->
                "I can automatically create a Support Ticket for our Admin team to inspect. Would you like me to submit your request as a ticket?"
            else ->
                "Hello! I am SpinBot, your Lucky Spin assistant. You can ask me about game rules, 1-25 number betting, wallets, jackpots, or request human support ticket creation!"
        }
    }
}
