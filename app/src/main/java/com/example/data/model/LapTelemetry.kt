package com.example.data.model

import java.util.Locale

data class LapTelemetry(
    val lapNumber: Int,
    val lapTimeSeconds: Float,
    val sector1Seconds: Float,
    val sector2Seconds: Float,
    val sector3Seconds: Float,
    val compound: TireCompound,
    val tireAge: Int,
    val fuelLoadKg: Float,
    val trackTempC: Float,
    val airTempC: Float,
    val isPitIn: Boolean = false,
    val isPitOut: Boolean = false,
    val isSafetyCar: Boolean = false,
    val isVirtualSafetyCar: Boolean = false,
    val isYellowFlag: Boolean = false,
    val isValidPace: Boolean = true,
    val fuelCorrectedLapTimeSeconds: Float,
    val telemetrySpeedTrapKph: Float,
    val note: String = ""
) {
    val formattedLapTime: String
        get() = formatLapTime(lapTimeSeconds)

    val formattedFuelCorrected: String
        get() = formatLapTime(fuelCorrectedLapTimeSeconds)

    companion object {
        fun formatLapTime(seconds: Float): String {
            if (seconds <= 0f) return "--:--.---"
            val minutes = (seconds / 60).toInt()
            val remainingSec = seconds - (minutes * 60)
            return String.format(Locale.US, "%d:%06.3f", minutes, remainingSec)
        }
    }
}
