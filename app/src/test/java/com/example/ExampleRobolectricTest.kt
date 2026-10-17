package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SimulationConfig
import com.example.data.model.TireCompound
import com.example.data.repository.RaceDataRepository
import com.example.ml.LapTimePredictor
import com.example.ml.StrategyOptimizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Race Strategist", appName)
    }

    @Test
    fun `predict lap time accounts for tire degradation`() {
        val circuit = RaceDataRepository.circuits.first()
        val freshTirePrediction = LapTimePredictor.predictLapTime(
            circuit = circuit,
            currentLap = 5,
            totalLaps = 53,
            compound = TireCompound.MEDIUM,
            tireAge = 1
        )
        val wornTirePrediction = LapTimePredictor.predictLapTime(
            circuit = circuit,
            currentLap = 25,
            totalLaps = 53,
            compound = TireCompound.MEDIUM,
            tireAge = 25
        )

        assertTrue("Worn tire degradation should exceed fresh tire degradation",
            wornTirePrediction.tireDegradationSeconds > freshTirePrediction.tireDegradationSeconds
        )
    }

    @Test
    fun `strategy optimizer ranks strategies by total race time`() {
        val circuit = RaceDataRepository.circuits.first()
        val config = SimulationConfig(totalLaps = 53, pitLaneLossSeconds = 23.5f)
        val strategies = StrategyOptimizer.simulateAllStrategies(circuit, config)

        assertTrue("Should return multiple strategy permutations", strategies.size >= 4)
        assertEquals("First ranked strategy should have 0 delta", 0.0f, strategies.first().deltaToBestSeconds, 0.001f)
        assertTrue("Optimal plan should have recommendation flag set", strategies.first().isRecommended)
    }
}
