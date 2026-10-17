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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RaceSession
import com.example.data.model.SimulationConfig
import com.example.data.model.StrategyResult
import com.example.data.model.WeatherCondition
import com.example.ui.components.StintTimelineBar
import com.example.ui.theme.CockpitMuted
import com.example.ui.theme.CockpitSubtle
import com.example.ui.theme.CockpitWhite
import com.example.ui.theme.F1Red
import com.example.ui.theme.FlagGreen
import com.example.ui.theme.FlagSafetyCar
import com.example.ui.theme.RaceSurface
import com.example.ui.theme.RaceSurfaceBorder
import com.example.ui.theme.RaceSurfaceElevated
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TireMediumYellow

@Composable
fun StrategyOptimizerTab(
    session: RaceSession,
    config: SimulationConfig,
    strategies: List<StrategyResult>,
    onUpdatePitLoss: (Float) -> Unit,
    onUpdateWeather: (WeatherCondition) -> Unit,
    onToggleSafetyCar: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Interactive Simulation Parameters Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RaceSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "STRATEGY SIMULATION CONTROLS",
                        color = CockpitWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Weather Condition Selector
                    Text("TRACK SURFACE & WEATHER", color = CockpitMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherCondition.values().forEach { weather ->
                            val isSelected = config.weatherCondition == weather
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("weather_${weather.name.lowercase()}")
                                    .background(
                                        if (isSelected) F1Red else RaceSurfaceElevated,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(1.dp, if (isSelected) F1Red else RaceSurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { onUpdateWeather(weather) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = weather.label.substringBefore(" "),
                                    color = CockpitWhite,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pit Lane Time Loss Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("NORMAL PIT LANE LOSS", color = CockpitMuted, fontSize = 11.sp)
                        Text(
                            text = String.format("%.1fs", config.pitLaneLossSeconds),
                            color = TelemetryCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = config.pitLaneLossSeconds,
                        onValueChange = { onUpdatePitLoss(it) },
                        valueRange = 17f..28f,
                        steps = 21,
                        colors = SliderDefaults.colors(
                            thumbColor = TelemetryCyan,
                            activeTrackColor = TelemetryCyan,
                            inactiveTrackColor = RaceSurfaceElevated
                        ),
                        modifier = Modifier.testTag("pit_loss_slider")
                    )

                    // Safety car impact notification
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (config.safetyCarLap != null) FlagSafetyCar.copy(alpha = 0.15f) else RaceSurfaceElevated,
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (config.safetyCarLap != null) FlagSafetyCar.copy(alpha = 0.5f) else RaceSurfaceBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onToggleSafetyCar() }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (config.safetyCarLap != null) "SAFETY CAR TRIGGERED (LAP ${config.safetyCarLap})" else "NO SAFETY CAR ACTIVE",
                                color = if (config.safetyCarLap != null) FlagSafetyCar else CockpitMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (config.safetyCarLap != null) "SC Pit Loss: ${config.safetyCarPitLossSeconds}s (-${config.pitLaneLossSeconds - config.safetyCarPitLossSeconds}s delta advantage)" else "Tap to simulate SC period",
                                color = CockpitSubtle,
                                fontSize = 10.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(if (config.safetyCarLap != null) FlagSafetyCar else RaceSurfaceBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (config.safetyCarLap != null) "ACTIVE" else "ENABLE",
                                color = CockpitWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "RANKED STRATEGY SIMULATIONS (${strategies.size} PERMUTATIONS)",
                color = CockpitMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // List of Ranked Strategies
        itemsIndexed(strategies) { index, strategy ->
            val isFirst = index == 0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("strategy_card_$index")
                    .background(RaceSurface, RoundedCornerShape(10.dp))
                    .border(
                        1.dp,
                        if (isFirst) FlagGreen.copy(alpha = 0.8f) else RaceSurfaceBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
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
                                    .background(if (isFirst) FlagGreen else RaceSurfaceElevated, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    color = CockpitWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
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
                            text = strategy.formattedDelta,
                            color = if (isFirst) FlagGreen else CockpitMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    StintTimelineBar(strategy = strategy, totalLaps = config.totalLaps)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total: ${strategy.formattedTotalTime} • ${strategy.stopCount} Stop(s)",
                            color = CockpitMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Risk: ${"★".repeat(strategy.riskScore)}${"☆".repeat(5 - strategy.riskScore)}",
                            color = if (strategy.riskScore >= 4) F1Red else TireMediumYellow,
                            fontSize = 11.sp
                        )
                    }

                    if (strategy.pitLaps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pit Lap(s): ${strategy.pitLaps.joinToString(", ")}",
                                color = TelemetryCyan,
                                fontSize = 11.sp
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

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
