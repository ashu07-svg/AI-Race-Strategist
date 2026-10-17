package com.example.ml

import com.example.data.model.GrandPrixCircuit
import com.example.data.model.SimulationConfig
import com.example.data.model.Stint
import com.example.data.model.StrategyResult
import com.example.data.model.TireCompound
import com.example.data.model.WeatherCondition
import kotlin.math.max
import kotlin.math.min

object StrategyOptimizer {

    data class CandidatePlan(
        val planName: String,
        val compounds: List<TireCompound>,
        val pitLaps: List<Int>,
        val summary: String
    )

    fun simulateAllStrategies(
        circuit: GrandPrixCircuit,
        config: SimulationConfig
    ): List<StrategyResult> {
        val totalLaps = config.totalLaps
        val candidates = generateCandidatePlans(totalLaps, config.weatherCondition, config.startingCompound)

        val results = candidates.map { candidate ->
            evaluateStrategy(circuit, config, candidate)
        }.sortedBy { it.totalRaceTimeSeconds }

        if (results.isEmpty()) return emptyList()

        val bestTime = results.first().totalRaceTimeSeconds
        return results.mapIndexed { index, item ->
            val delta = item.totalRaceTimeSeconds - bestTime
            item.copy(
                deltaToBestSeconds = delta,
                isRecommended = index == 0
            )
        }
    }

    private fun generateCandidatePlans(
        totalLaps: Int,
        weather: WeatherCondition,
        startCompound: TireCompound
    ): List<CandidatePlan> {
        val plans = mutableListOf<CandidatePlan>()

        if (weather == WeatherCondition.WET) {
            // Full Wet scenarios
            val pit1 = (totalLaps * 0.45f).toInt()
            plans.add(CandidatePlan("Plan A (Wet 1-Stop): Wet -> Wet", listOf(TireCompound.WET, TireCompound.WET), listOf(pit1), "Standard 1-stop preservation on heavy wet"))
            val pitA = (totalLaps * 0.32f).toInt()
            val pitB = (totalLaps * 0.68f).toInt()
            plans.add(CandidatePlan("Plan B (Wet 2-Stop): Wet -> Wet -> Inter", listOf(TireCompound.WET, TireCompound.WET, TireCompound.INTERMEDIATE), listOf(pitA, pitB), "Aggressive drying track transition"))
            return plans
        }

        if (weather == WeatherCondition.DAMP) {
            // Mixed conditions
            val pit1 = (totalLaps * 0.40f).toInt()
            plans.add(CandidatePlan("Plan A (Mixed): Inter -> Medium", listOf(TireCompound.INTERMEDIATE, TireCompound.MEDIUM), listOf(pit1), "Crossover from damp to dry slick"))
            val pitA = (totalLaps * 0.30f).toInt()
            val pitB = (totalLaps * 0.65f).toInt()
            plans.add(CandidatePlan("Plan B (Mixed 2-Stop): Inter -> Inter -> Soft", listOf(TireCompound.INTERMEDIATE, TireCompound.INTERMEDIATE, TireCompound.SOFT), listOf(pitA, pitB), "Persistent rain with late drying sprint"))
            return plans
        }

        // --- Dry Race Permutations ---
        // 1-Stop Strategies (Satisfying two-compound rule)
        val m1 = (totalLaps * 0.38f).toInt() // Medium stint ~38%
        plans.add(CandidatePlan("Plan A (1-Stop): Medium -> Hard", listOf(TireCompound.MEDIUM, TireCompound.HARD), listOf(m1), "Standard conservative 1-stop with great tire delta"))

        val s1 = (totalLaps * 0.28f).toInt() // Soft stint ~28%
        plans.add(CandidatePlan("Plan B (1-Stop): Soft -> Hard", listOf(TireCompound.SOFT, TireCompound.HARD), listOf(s1), "Aggressive launch on Softs, early transition to long Hard stint"))

        val h1 = (totalLaps * 0.62f).toInt() // Hard first
        plans.add(CandidatePlan("Plan C (1-Stop): Hard -> Medium", listOf(TireCompound.HARD, TireCompound.MEDIUM), listOf(h1), "Alternative overcut strategy. Long opening stint waiting for late SC"))

        val sM1 = (totalLaps * 0.34f).toInt()
        plans.add(CandidatePlan("Plan D (1-Stop): Medium -> Soft", listOf(TireCompound.MEDIUM, TireCompound.SOFT), listOf(totalLaps - sM1), "Late charge on high-grip soft compound"))

        // 2-Stop Strategies
        val s2A = (totalLaps * 0.25f).toInt()
        val s2B = (totalLaps * 0.58f).toInt()
        plans.add(CandidatePlan("Plan E (2-Stop): Soft -> Hard -> Medium", listOf(TireCompound.SOFT, TireCompound.HARD, TireCompound.MEDIUM), listOf(s2A, s2B), "Attacking 2-stop. High maximum pace with flexible pit windows"))

        val m2A = (totalLaps * 0.30f).toInt()
        val m2B = (totalLaps * 0.65f).toInt()
        plans.add(CandidatePlan("Plan F (2-Stop): Medium -> Hard -> Medium", listOf(TireCompound.MEDIUM, TireCompound.HARD, TireCompound.MEDIUM), listOf(m2A, m2B), "Balanced 2-stop keeping optimal tire degradation curve"))

        val shsA = (totalLaps * 0.22f).toInt()
        val shsB = (totalLaps * 0.68f).toInt()
        plans.add(CandidatePlan("Plan G (2-Stop): Soft -> Hard -> Soft", listOf(TireCompound.SOFT, TireCompound.HARD, TireCompound.SOFT), listOf(shsA, shsB), "Double soft sprint stint. Vulnerable to safety car timing"))

        // 3-Stop Strategy (Extreme degradation sprint)
        val s3A = (totalLaps * 0.20f).toInt()
        val s3B = (totalLaps * 0.46f).toInt()
        val s3C = (totalLaps * 0.72f).toInt()
        plans.add(CandidatePlan("Plan H (3-Stop): Soft -> Medium -> Medium -> Soft", listOf(TireCompound.SOFT, TireCompound.MEDIUM, TireCompound.MEDIUM, TireCompound.SOFT), listOf(s3A, s3B, s3C), "Hyper-aggressive 3-stop sprint. Requires maximum overtaking pace"))

        return plans
    }

    private fun evaluateStrategy(
        circuit: GrandPrixCircuit,
        config: SimulationConfig,
        candidate: CandidatePlan
    ): StrategyResult {
        val totalLaps = config.totalLaps
        val stints = mutableListOf<Stint>()
        val pitLaps = candidate.pitLaps

        // Build stints
        var curStart = 1
        for (i in candidate.compounds.indices) {
            val endLap = if (i < pitLaps.size) pitLaps[i] else totalLaps
            stints.add(Stint(stintIndex = i + 1, compound = candidate.compounds[i], startLap = curStart, endLap = endLap))
            curStart = endLap + 1
        }

        var totalTime = 0.0f
        var totalPitLoss = 0.0f
        var maxRiskLevel = 1

        // Lap by lap simulation
        for (stint in stints) {
            var stintTireAge = 1
            for (lap in stint.startLap..stint.endLap) {
                // Determine if safety car is active on this lap
                val isSafetyCarLap = config.safetyCarLap != null && lap in config.safetyCarLap..(config.safetyCarLap + 3)
                val isVirtualSafetyCarLap = config.virtualSafetyCarLap != null && lap in config.virtualSafetyCarLap..(config.virtualSafetyCarLap + 2)

                val prediction = LapTimePredictor.predictLapTime(
                    circuit = circuit,
                    currentLap = lap,
                    totalLaps = totalLaps,
                    compound = stint.compound,
                    tireAge = stintTireAge,
                    trackTempC = circuit.defaultWeather.trackTempC + config.trackTempOffsetC,
                    weatherCondition = config.weatherCondition,
                    inTraffic = false
                )

                var lapTime = prediction.predictedLapTimeSeconds
                if (isSafetyCarLap) {
                    lapTime += 32.0f // delta pace under SC
                } else if (isVirtualSafetyCarLap) {
                    lapTime += 18.0f // delta pace under VSC
                }

                totalTime += lapTime
                stintTireAge++
            }

            // Risk check: did stint exceed cliff lap?
            val stintLen = stint.laps
            if (stintLen > stint.compound.cliffLap) {
                maxRiskLevel = max(maxRiskLevel, min(5, 3 + (stintLen - stint.compound.cliffLap) / 3))
            }
        }

        // Add pit stop losses
        for (pitLap in pitLaps) {
            val isPitUnderSC = config.safetyCarLap != null && pitLap in config.safetyCarLap..(config.safetyCarLap + 3)
            val isPitUnderVSC = config.virtualSafetyCarLap != null && pitLap in config.virtualSafetyCarLap..(config.virtualSafetyCarLap + 2)

            val loss = when {
                isPitUnderSC -> config.safetyCarPitLossSeconds // e.g. 11.5s
                isPitUnderVSC -> config.safetyCarPitLossSeconds + 2.5f // ~14s
                else -> config.pitLaneLossSeconds // e.g. 22.0s
            }
            totalPitLoss += loss
            totalTime += loss
        }

        val avgLap = totalTime / totalLaps
        val undercutWindow = if (pitLaps.isNotEmpty()) "Laps ${pitLaps.first() - 2} to ${pitLaps.first() + 1}" else "N/A"

        return StrategyResult(
            planId = candidate.planName.take(6),
            planName = candidate.planName,
            stopCount = candidate.compounds.size - 1,
            stints = stints,
            pitLaps = pitLaps,
            totalRaceTimeSeconds = totalTime,
            deltaToBestSeconds = 0.0f,
            averageLapTimeSeconds = avgLap,
            totalPitLossSeconds = totalPitLoss,
            riskScore = maxRiskLevel,
            strategySummary = candidate.summary,
            undercutWindow = undercutWindow
        )
    }
}
