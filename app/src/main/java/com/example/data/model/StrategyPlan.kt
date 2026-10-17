package com.example.data.model

import java.util.Locale

data class Stint(
    val stintIndex: Int,
    val compound: TireCompound,
    val startLap: Int,
    val endLap: Int,
    val laps: Int = endLap - startLap + 1,
    val isFreshTire: Boolean = true
) {
    val description: String
        get() = "${compound.fullName} (Laps $startLap - $endLap: $laps laps)"
}

data class StrategyResult(
    val planId: String,
    val planName: String, // e.g. "Plan A: M -> H (1-Stop)"
    val stopCount: Int,
    val stints: List<Stint>,
    val pitLaps: List<Int>,
    val totalRaceTimeSeconds: Float,
    val deltaToBestSeconds: Float,
    val averageLapTimeSeconds: Float,
    val totalPitLossSeconds: Float,
    val riskScore: Int, // 1 (Safe) to 5 (High Risk / Edge of cliff)
    val strategySummary: String,
    val isRecommended: Boolean = false,
    val undercutWindow: String = ""
) {
    val formattedTotalTime: String
        get() {
            val totalSeconds = totalRaceTimeSeconds.toInt()
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            val millis = ((totalRaceTimeSeconds - totalSeconds) * 1000).toInt()
            return String.format(Locale.US, "%d:%02d:%02d.%03d", hours, minutes, seconds, millis)
        }

    val formattedDelta: String
        get() = if (deltaToBestSeconds <= 0.001f) "OPTIMAL (0.000s)" else String.format(Locale.US, "+%.3fs", deltaToBestSeconds)

    val formattedAvgLap: String
        get() = LapTelemetry.formatLapTime(averageLapTimeSeconds)
}

data class SimulationConfig(
    val totalLaps: Int = 53,
    val baseQualifyingLapTime: Float = 81.5f,
    val pitLaneLossSeconds: Float = 22.0f,
    val safetyCarPitLossSeconds: Float = 11.5f,
    val weatherCondition: WeatherCondition = WeatherCondition.DRY,
    val safetyCarLap: Int? = null, // e.g. Lap 24
    val virtualSafetyCarLap: Int? = null,
    val trackTempOffsetC: Float = 0f,
    val trafficPenaltyPerLapSeconds: Float = 0.25f,
    val startingCompound: TireCompound = TireCompound.MEDIUM,
    val availableCompounds: List<TireCompound> = listOf(TireCompound.SOFT, TireCompound.MEDIUM, TireCompound.HARD)
)
