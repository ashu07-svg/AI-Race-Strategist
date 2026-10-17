package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TireHardWhite
import com.example.ui.theme.TireInterGreen
import com.example.ui.theme.TireMediumYellow
import com.example.ui.theme.TireSoftRed
import com.example.ui.theme.TireWetBlue

enum class TireCompound(
    val code: String,
    val fullName: String,
    val basePaceDeltaSeconds: Float, // Relative to Soft
    val linearWearRate: Float,       // Seconds per lap of tire age
    val quadraticWearRate: Float,    // Nonlinear degradation
    val cliffLap: Int,               // Sudden degradation threshold
    val maxRecommendedLaps: Int,
    val color: Color
) {
    SOFT(
        code = "S",
        fullName = "Soft (C4/C5)",
        basePaceDeltaSeconds = 0.0f,
        linearWearRate = 0.095f,
        quadraticWearRate = 0.0035f,
        cliffLap = 18,
        maxRecommendedLaps = 22,
        color = TireSoftRed
    ),
    MEDIUM(
        code = "M",
        fullName = "Medium (C3)",
        basePaceDeltaSeconds = 0.42f,
        linearWearRate = 0.055f,
        quadraticWearRate = 0.0018f,
        cliffLap = 28,
        maxRecommendedLaps = 35,
        color = TireMediumYellow
    ),
    HARD(
        code = "H",
        fullName = "Hard (C2)",
        basePaceDeltaSeconds = 0.92f,
        linearWearRate = 0.032f,
        quadraticWearRate = 0.0009f,
        cliffLap = 42,
        maxRecommendedLaps = 48,
        color = TireHardWhite
    ),
    INTERMEDIATE(
        code = "I",
        fullName = "Intermediate",
        basePaceDeltaSeconds = 4.80f,
        linearWearRate = 0.070f,
        quadraticWearRate = 0.0025f,
        cliffLap = 26,
        maxRecommendedLaps = 32,
        color = TireInterGreen
    ),
    WET(
        code = "W",
        fullName = "Full Wet",
        basePaceDeltaSeconds = 9.50f,
        linearWearRate = 0.050f,
        quadraticWearRate = 0.0015f,
        cliffLap = 35,
        maxRecommendedLaps = 40,
        color = TireWetBlue
    );

    /**
     * Calculates tire-specific degradation penalty in seconds given tire age.
     */
    fun getDegradation(tireAge: Int): Float {
        val linear = linearWearRate * tireAge
        val quadratic = quadraticWearRate * tireAge * tireAge
        val cliffPenalty = if (tireAge > cliffLap) {
            val overage = tireAge - cliffLap
            0.12f * overage * overage
        } else {
            0.0f
        }
        return linear + quadratic + cliffPenalty
    }
}
