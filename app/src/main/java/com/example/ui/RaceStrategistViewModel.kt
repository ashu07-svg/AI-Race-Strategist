package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiRaceStrategist
import com.example.ai.PitWallRecommendation
import com.example.data.model.LapTelemetry
import com.example.data.model.ModelEvaluation
import com.example.data.model.RaceSession
import com.example.data.model.SimulationConfig
import com.example.data.model.StrategyResult
import com.example.data.model.WeatherCondition
import com.example.data.repository.RaceDataRepository
import com.example.ml.LapTimePredictor
import com.example.ml.StrategyOptimizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RaceStrategistUiState(
    val session: RaceSession,
    val config: SimulationConfig,
    val laps: List<LapTelemetry> = emptyList(),
    val strategies: List<StrategyResult> = emptyList(),
    val modelEvaluation: ModelEvaluation = ModelEvaluation(),
    val pitWallRecommendation: PitWallRecommendation? = null,
    val isAiLoading: Boolean = false,
    val isSafetyCarActive: Boolean = false,
    val activeTab: AppTab = AppTab.OVERVIEW
)

enum class AppTab(val title: String) {
    OVERVIEW("Overview"),
    STRATEGY("Strategy"),
    TIRES("Tires"),
    ML_PREDICT("Predict"),
    EVALUATION("Model"),
    DATA("Data")
}

class RaceStrategistViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        RaceStrategistUiState(
            session = RaceDataRepository.createSession("monza", "VER", isSynthetic = false),
            config = SimulationConfig(totalLaps = 53, pitLaneLossSeconds = 23.5f)
        )
    )
    val uiState: StateFlow<RaceStrategistUiState> = _uiState.asStateFlow()

    init {
        recalculateRaceState()
        consultAi(null)
    }

    fun selectCircuit(circuitId: String) {
        val newSession = RaceDataRepository.createSession(
            circuitId = circuitId,
            driverCode = _uiState.value.session.driver.code,
            isSynthetic = _uiState.value.session.isSynthetic
        )
        _uiState.update {
            it.copy(
                session = newSession,
                config = it.config.copy(
                    totalLaps = newSession.circuit.totalLaps,
                    pitLaneLossSeconds = newSession.circuit.averagePitLaneLossSeconds,
                    safetyCarPitLossSeconds = newSession.circuit.safetyCarPitLossSeconds,
                    safetyCarLap = null
                ),
                isSafetyCarActive = false
            )
        }
        recalculateRaceState()
        consultAi("New circuit selected: ${newSession.circuit.name}. Evaluate opening stint strategy.")
    }

    fun selectDriver(driverCode: String) {
        val newSession = RaceDataRepository.createSession(
            circuitId = _uiState.value.session.circuit.id,
            driverCode = driverCode,
            isSynthetic = _uiState.value.session.isSynthetic
        )
        _uiState.update { it.copy(session = newSession) }
        recalculateRaceState()
    }

    fun toggleDemoMode() {
        val newSyntheticState = !_uiState.value.session.isSynthetic
        val newSession = _uiState.value.session.copy(
            isSynthetic = newSyntheticState,
            dataProvenance = if (newSyntheticState) "Synthetic High-Fidelity Simulation" else "FastF1 Telemetry Archive (Synced & Validated)"
        )
        _uiState.update { it.copy(session = newSession) }
        recalculateRaceState()
    }

    fun toggleSafetyCar() {
        val scActive = !_uiState.value.isSafetyCarActive
        val scLap = if (scActive) 24 else null
        _uiState.update {
            it.copy(
                isSafetyCarActive = scActive,
                config = it.config.copy(safetyCarLap = scLap)
            )
        }
        recalculateRaceState()
        consultAi(if (scActive) "SAFETY CAR DEPLOYED! Should we box right now for a cheap pit stop?" else null)
    }

    fun updatePitLoss(seconds: Float) {
        _uiState.update {
            it.copy(config = it.config.copy(pitLaneLossSeconds = seconds))
        }
        recalculateRaceState()
    }

    fun updateWeather(condition: WeatherCondition) {
        _uiState.update {
            it.copy(config = it.config.copy(weatherCondition = condition))
        }
        recalculateRaceState()
        consultAi("Weather conditions changed to ${condition.label}. Recalculate crossover window.")
    }

    fun switchTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun consultAi(customQuery: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            val state = _uiState.value
            val recommendation = GeminiRaceStrategist.consultPitWallAi(
                session = state.session,
                currentLap = if (state.isSafetyCarActive) 24 else 18,
                activeCompound = "Medium (C3)",
                tireAge = 18,
                weather = state.config.weatherCondition,
                safetyCarActive = state.isSafetyCarActive,
                strategies = state.strategies,
                customQuery = customQuery
            )
            _uiState.update {
                it.copy(
                    pitWallRecommendation = recommendation,
                    isAiLoading = false
                )
            }
        }
    }

    private fun recalculateRaceState() {
        val session = _uiState.value.session
        val config = _uiState.value.config
        val laps = RaceDataRepository.loadLapTelemetry(
            session = session,
            safetyCarLap = config.safetyCarLap,
            weatherCondition = config.weatherCondition
        )
        val strategies = StrategyOptimizer.simulateAllStrategies(
            circuit = session.circuit,
            config = config
        )
        val eval = LapTimePredictor.getModelEvaluationMetrics()

        _uiState.update {
            it.copy(
                laps = laps,
                strategies = strategies,
                modelEvaluation = eval
            )
        }
    }
}
