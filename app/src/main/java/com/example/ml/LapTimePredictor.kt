package com.example.ml

import com.example.data.model.GrandPrixCircuit
import com.example.data.model.LapTelemetry
import com.example.data.model.ModelEvaluation
import com.example.data.model.TireCompound
import com.example.data.model.WeatherCondition
import kotlin.math.max
import kotlin.math.sqrt

object LapTimePredictor {

    const val FUEL_BURN_RATE_KG_PER_LAP = 1.95f
    const val FUEL_TIME_PENALTY_PER_KG = 0.033f // seconds per kg of fuel
    const val BASELINE_TRACK_TEMP_C = 35.0f
    const val TRACK_TEMP_SENSITIVITY = 0.018f // seconds per deg C above baseline

    /**
     * Physics-informed regression model for estimating instantaneous lap time.
     */
    fun predictLapTime(
        circuit: GrandPrixCircuit,
        currentLap: Int,
        totalLaps: Int,
        compound: TireCompound,
        tireAge: Int,
        trackTempC: Float = circuit.defaultWeather.trackTempC,
        weatherCondition: WeatherCondition = circuit.defaultWeather.condition,
        inTraffic: Boolean = false,
        trafficDelaySeconds: Float = 0.35f
    ): PredictionResult {
        val basePace = circuit.baseQualifyingLapTime + 2.8f // Race fuel/engine mode pace offset

        // 1. Compound baseline delta
        val compoundDelta = compound.basePaceDeltaSeconds

        // 2. Tire Degradation (Linear + Quadratic + Cliff threshold)
        val degradationSeconds = compound.getDegradation(tireAge)

        // 3. Fuel Mass burn-off effect (heaviest at lap 1, lightest at finish)
        val remainingLaps = max(0, totalLaps - currentLap)
        val fuelLoadKg = remainingLaps * FUEL_BURN_RATE_KG_PER_LAP
        val initialFuelKg = totalLaps * FUEL_BURN_RATE_KG_PER_LAP
        val fuelConsumedKg = initialFuelKg - fuelLoadKg
        val fuelPaceAdvantage = fuelConsumedKg * FUEL_TIME_PENALTY_PER_KG

        // 4. Track Temperature effect
        val tempDelta = (trackTempC - BASELINE_TRACK_TEMP_C) * TRACK_TEMP_SENSITIVITY

        // 5. Weather grip factor
        val weatherPenalty = when (weatherCondition) {
            WeatherCondition.DRY -> if (compound == TireCompound.INTERMEDIATE || compound == TireCompound.WET) 5.5f else 0.0f
            WeatherCondition.DAMP -> if (compound == TireCompound.INTERMEDIATE) 0.0f else if (compound == TireCompound.WET) 2.5f else 6.0f
            WeatherCondition.WET -> if (compound == TireCompound.WET) 0.0f else if (compound == TireCompound.INTERMEDIATE) 3.5f else 15.0f
        }

        // 6. Traffic & dirty air
        val trafficPenalty = if (inTraffic) trafficDelaySeconds else 0.0f

        // Estimated lap time
        val predictedTime = basePace + compoundDelta + degradationSeconds - fuelPaceAdvantage + tempDelta + weatherPenalty + trafficPenalty

        // Fuel-corrected pace (normalized to zero-fuel reference to isolate tire wear)
        val fuelCorrectedPace = predictedTime + fuelPaceAdvantage

        // Uncertainty margin (expands on high tire age or extreme weather)
        val uncertainty = 0.15f + (tireAge * 0.008f) + if (weatherCondition != WeatherCondition.DRY) 0.45f else 0.0f

        return PredictionResult(
            predictedLapTimeSeconds = predictedTime,
            fuelCorrectedLapTimeSeconds = fuelCorrectedPace,
            fuelLoadKg = fuelLoadKg,
            tireDegradationSeconds = degradationSeconds,
            fuelAdvantageSeconds = fuelPaceAdvantage,
            uncertaintyMarginSeconds = uncertainty
        )
    }

    fun getModelEvaluationMetrics(): ModelEvaluation {
        return ModelEvaluation()
    }
}

data class PredictionResult(
    val predictedLapTimeSeconds: Float,
    val fuelCorrectedLapTimeSeconds: Float,
    val fuelLoadKg: Float,
    val tireDegradationSeconds: Float,
    val fuelAdvantageSeconds: Float,
    val uncertaintyMarginSeconds: Float
)
