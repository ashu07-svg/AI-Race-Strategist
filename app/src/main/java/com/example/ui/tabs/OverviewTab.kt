package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.PitWallRecommendation
import com.example.data.model.LapTelemetry
import com.example.data.model.RaceSession
import com.example.data.model.StrategyResult
import com.example.data.repository.RaceDataRepository
import com.example.ui.components.LapPaceChart
import com.example.ui.components.MetricCard
import com.example.ui.components.PitWallRadioCard
import com.example.ui.components.StintTimelineBar
import com.example.ui.theme.CockpitMuted
import com.example.ui.theme.CockpitSubtle
import com.example.ui.theme.CockpitWhite
import com.example.ui.theme.F1Red
import com.example.ui.theme.FlagGreen
import com.example.ui.theme.RaceSurface
import com.example.ui.theme.RaceSurfaceBorder
import com.example.ui.theme.RaceSurfaceElevated
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TireMediumYellow

@Composable
fun OverviewTab(
    session: RaceSession,
    laps: List<LapTelemetry>,
    strategies: List<StrategyResult>,
    pitWallAi: PitWallRecommendation?,
    isAiLoading: Boolean,
    onConsultAi: (String?) -> Unit,
    onSelectCircuit: (String) -> Unit,
    onSelectDriver: (String) -> Unit,
    onOpenSessionSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val optimalStrategy = strategies.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Quick Circuit Selector with Session Config button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GRAND PRIX CIRCUIT",
                    color = CockpitMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "SESSION & DATA SETTINGS ⚙",
                    color = TelemetryCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .testTag("overview_open_session_selector")
                        .clickable { onOpenSessionSelector() }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RaceDataRepository.circuits) { circuit ->
                    val isSelected = circuit.id == session.circuit.id
                    Box(
                        modifier = Modifier
                            .testTag("circuit_select_${circuit.id}")
                            .background(
                                if (isSelected) F1Red else RaceSurface,
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isSelected) F1Red else RaceSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectCircuit(circuit.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = circuit.name.substringBefore(" ("),
                            color = CockpitWhite,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Key Performance Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Optimal Race Time",
                    value = optimalStrategy?.formattedTotalTime ?: "--:--:--",
                    subtitle = optimalStrategy?.planName ?: "Calculating...",
                    accentColor = TelemetryCyan,
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f),
                    testTag = "optimal_time_metric"
                )
                MetricCard(
                    title = "Avg Lap Pace",
                    value = optimalStrategy?.formattedAvgLap ?: "--:--.---",
                    subtitle = "${session.circuit.totalLaps} Laps Total",
                    accentColor = TireMediumYellow,
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f),
                    testTag = "avg_pace_metric"
                )
            }
        }

        // Secondary metrics row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Strategy Type",
                    value = "${optimalStrategy?.stopCount ?: 1} STOP",
                    subtitle = "Pit Loss: ${optimalStrategy?.totalPitLossSeconds?.toInt() ?: 22}s",
                    accentColor = FlagGreen,
                    icon = Icons.Default.Flag,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Tire Deg Severity",
                    value = session.circuit.tireDegradationSeverity.uppercase(),
                    subtitle = "Overtake: ${session.circuit.overtakingDifficulty}",
                    accentColor = F1Red,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Gemini AI Pit Wall Radio Briefing
        item {
            PitWallRadioCard(
                recommendation = pitWallAi,
                isLoading = isAiLoading,
                onConsultAi = onConsultAi
            )
        }

        // Optimal Strategy Card
        item {
            optimalStrategy?.let { strategy ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaceSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(F1Red, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("RANK #1", color = CockpitWhite, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strategy.planName,
                                    color = CockpitWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "DELTA: 0.000s",
                                color = FlagGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        StintTimelineBar(strategy = strategy, totalLaps = session.circuit.totalLaps)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strategy.strategySummary,
                            color = CockpitMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pit Windows: ${if (strategy.pitLaps.isNotEmpty()) "Lap ${strategy.pitLaps.joinToString(", ")}" else "None"}",
                                color = TelemetryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Undercut: ${strategy.undercutWindow}",
                                color = CockpitSubtle,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Lap Pace Chart
        item {
            LapPaceChart(laps = laps)
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
