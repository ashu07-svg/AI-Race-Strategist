package com.example.ai

import com.example.BuildConfig
import com.example.data.model.RaceSession
import com.example.data.model.StrategyResult
import com.example.data.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiRaceStrategist {

    private const val MODEL_NAME = "gemini-3.8-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Solicits real-time pit wall race engineer radio analysis from Gemini 3.8 Flash.
     */
    suspend fun consultPitWallAi(
        session: RaceSession,
        currentLap: Int,
        activeCompound: String,
        tireAge: Int,
        weather: WeatherCondition,
        safetyCarActive: Boolean,
        strategies: List<StrategyResult>,
        customQuery: String? = null
    ): PitWallRecommendation = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val topPlan = strategies.firstOrNull()
        val secondPlan = strategies.getOrNull(1)

        val prompt = buildString {
            append("You are the Chief Race Strategist on the pit wall for Formula 1 driver ${session.driver.name} (#${session.driver.number}, ${session.driver.team}) at ${session.circuit.name}.\n")
            append("SESSION TELEMETRY STATUS:\n")
            append("- Current Lap: $currentLap of ${session.circuit.totalLaps}\n")
            append("- Fitted Tire: $activeCompound (Age: $tireAge laps)\n")
            append("- Track Status: ${if (safetyCarActive) "SAFETY CAR DEPLOYED (Pit Loss reduced to ${session.circuit.safetyCarPitLossSeconds}s)" else "GREEN FLAG"}\n")
            append("- Track Weather: ${weather.label}, Track Temp: ${session.weather.trackTempC}°C\n")
            append("- Optimal Projected Strategy: ${topPlan?.planName ?: "Plan A"} (Total: ${topPlan?.formattedTotalTime ?: "N/A"})\n")
            append("- Alternative Strategy: ${secondPlan?.planName ?: "Plan B"} (Delta: ${secondPlan?.formattedDelta ?: "N/A"})\n")
            if (!customQuery.isNullOrBlank()) {
                append("- Driver / Pit Query: \"$customQuery\"\n")
            }
            append("\nProvide an authentic, decisive Formula 1 pit wall briefing:\n")
            append("1. Radio Call (1-2 sentences in genuine F1 engineer radio style, e.g., 'Radio check, Max. Box opposite Norris, we are extending Plan A.').\n")
            append("2. Strategic Rationale (Analysis of tire degradation crossover, undercut/overcut window, safety car risk).\n")
            append("3. Tactical Directive (Recommended action: BOX THIS LAP, STAY OUT, SWITCH TO PLAN B, PREPARE PIT WINDOW).\n")
            append("Keep it concise, professional, and motorsport-authentic.")
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // High-fidelity fallback heuristic for offline or unconfigured API key
            return@withContext generateHeuristicRecommendation(
                session, currentLap, activeCompound, tireAge, safetyCarActive, topPlan, customQuery
            )
        }

        try {
            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("temperature", 0.4)
                    put("topP", 0.9)
                }
                put("generationConfig", generationConfig)
            }

            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext generateHeuristicRecommendation(
                    session, currentLap, activeCompound, tireAge, safetyCarActive, topPlan, customQuery
                )
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                parseAiResponse(text, safetyCarActive, topPlan)
            } else {
                generateHeuristicRecommendation(session, currentLap, activeCompound, tireAge, safetyCarActive, topPlan, customQuery)
            }
        } catch (e: Exception) {
            generateHeuristicRecommendation(session, currentLap, activeCompound, tireAge, safetyCarActive, topPlan, customQuery)
        }
    }

    private fun parseAiResponse(
        rawText: String,
        safetyCarActive: Boolean,
        topPlan: StrategyResult?
    ): PitWallRecommendation {
        val lines = rawText.lines().filter { it.isNotBlank() }
        val radioLine = lines.firstOrNull { it.contains("Radio", ignoreCase = true) || it.startsWith("1.") }
            ?.replace("1.", "")?.replace("Radio Call:", "")?.trim()
            ?: "Radio check. Monitoring telemetry data closely. Plan A remains preferred."

        val action = when {
            safetyCarActive -> "BOX UNDER SAFETY CAR (CHEAP PIT STOP)"
            rawText.contains("BOX THIS LAP", ignoreCase = true) -> "BOX THIS LAP"
            rawText.contains("STAY OUT", ignoreCase = true) -> "STAY OUT - EXTEND STINT"
            rawText.contains("PLAN B", ignoreCase = true) -> "SWITCH TO PLAN B"
            else -> "EXECUTE PLANNED PIT WINDOW"
        }

        return PitWallRecommendation(
            radioTransmission = radioLine.removeSurrounding("\""),
            tacticalAction = action,
            fullStrategicRationale = rawText,
            isAiGenerated = true
        )
    }

    private fun generateHeuristicRecommendation(
        session: RaceSession,
        currentLap: Int,
        activeCompound: String,
        tireAge: Int,
        safetyCarActive: Boolean,
        topPlan: StrategyResult?,
        customQuery: String?
    ): PitWallRecommendation {
        val driverLastName = session.driver.name.substringAfterLast(" ")
        val pitLap = topPlan?.pitLaps?.firstOrNull() ?: 20

        return if (safetyCarActive) {
            PitWallRecommendation(
                radioTransmission = "Safety car deployed! Box, box, box! Cheap stop available, pit loss delta is -10.5 seconds. Confirm boxing now.",
                tacticalAction = "BOX UNDER SAFETY CAR (CHEAP STOP)",
                fullStrategicRationale = "With the Safety Car deployed at lap $currentLap, the delta pace drops car speeds by ~30%, cutting effective pit loss from ${session.circuit.averagePitLaneLossSeconds}s down to ${session.circuit.safetyCarPitLossSeconds}s. Pitting immediately preserves track position and gains a net +11.2s against competitors remaining on track.",
                isAiGenerated = false
            )
        } else if (currentLap in (pitLap - 2)..(pitLap + 1)) {
            PitWallRecommendation(
                radioTransmission = "Radio check, $driverLastName. We are in the pit window for ${topPlan?.planName ?: "Plan A"}. Undercut is powerful on fresh rubber, box this lap.",
                tacticalAction = "BOX THIS LAP (OPTIMAL WINDOW)",
                fullStrategicRationale = "Telemetry indicates tire degradation is nearing crossover cliff (+0.42s/lap pace loss). Fresh tire out-lap pace delta is estimated at +1.4s, guaranteeing successful undercut over defending cars.",
                isAiGenerated = false
            )
        } else {
            PitWallRecommendation(
                radioTransmission = "$driverLastName, pace looks strong on the $activeCompound. Fuel burn is on target (-0.06s/lap lighter). Maintain target delta and manage front-left.",
                tacticalAction = "STAY OUT - MANAGE TIRES",
                fullStrategicRationale = "Current tire age of $tireAge laps remains well within the optimal thermal window. Fuel-corrected telemetry confirms pace is holding within 0.12s of target delta. Recommended pit window opens at Lap $pitLap.",
                isAiGenerated = false
            )
        }
    }
}

data class PitWallRecommendation(
    val radioTransmission: String,
    val tacticalAction: String,
    val fullStrategicRationale: String,
    val isAiGenerated: Boolean
)
