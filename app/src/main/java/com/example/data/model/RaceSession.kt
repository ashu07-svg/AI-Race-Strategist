package com.example.data.model

data class Driver(
    val code: String,
    val number: Int,
    val name: String,
    val team: String,
    val teamColorHex: Long,
    val baselinePaceOffset: Float // Delta to benchmark pace
)

data class GrandPrixCircuit(
    val id: String,
    val name: String,
    val location: String,
    val totalLaps: Int,
    val circuitLengthKm: Float,
    val baseQualifyingLapTime: Float, // in seconds
    val averagePitLaneLossSeconds: Float, // normal pit loss
    val safetyCarPitLossSeconds: Float,  // pit loss under SC / VSC
    val tireDegradationSeverity: String, // "High", "Medium", "Low"
    val overtakingDifficulty: String,    // "High", "Medium", "Low"
    val defaultWeather: SessionWeather
)

data class SessionWeather(
    val condition: WeatherCondition,
    val airTempC: Float,
    val trackTempC: Float,
    val humidityPct: Int,
    val rainProbabilityPct: Int
)

enum class WeatherCondition(val label: String) {
    DRY("Dry Track"),
    DAMP("Damp / Mixed"),
    WET("Heavy Rain / Wet")
}

data class RaceSession(
    val championship: String = "Formula 1",
    val season: Int = 2024,
    val round: Int,
    val circuit: GrandPrixCircuit,
    val sessionType: String = "Race",
    val driver: Driver,
    val weather: SessionWeather,
    val isSynthetic: Boolean = false,
    val dataProvenance: String = "FastF1 Telemetry Archive (Synced & Validated)"
)
