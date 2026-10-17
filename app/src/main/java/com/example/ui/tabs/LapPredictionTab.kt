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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LapTelemetry
import com.example.data.model.RaceSession
import com.example.data.model.TireCompound
import com.example.ml.LapTimePredictor
import com.example.ui.theme.CockpitMuted
import com.example.ui.theme.CockpitSubtle
import com.example.ui.theme.CockpitWhite
import com.example.ui.theme.F1Red
import com.example.ui.theme.FlagGreen
import com.example.ui.theme.RaceBackground
import com.example.ui.theme.RaceSurface
import com.example.ui.theme.RaceSurfaceBorder
import com.example.ui.theme.RaceSurfaceElevated
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TireMediumYellow
import java.util.Locale

@Composable
fun LapPredictionTab(
    session: RaceSession,
    modifier: Modifier = Modifier
) {
    var selectedCompound by remember { mutableStateOf(TireCompound.MEDIUM) }
    var currentLap by remember { mutableIntStateOf(15) }
    var tireAge by remember { mutableIntStateOf(15) }
    var trackTemp by remember { mutableFloatStateOf(session.weather.trackTempC) }
    var inTraffic by remember { mutableStateOf(false) }

    val totalLaps = session.circuit.totalLaps

    // Evaluate prediction
    val prediction = LapTimePredictor.predictLapTime(
        circuit = session.circuit,
        currentLap = currentLap,
        totalLaps = totalLaps,
        compound = selectedCompound,
        tireAge = tireAge,
        trackTempC = trackTemp,
        weatherCondition = session.weather.condition,
        inTraffic = inTraffic
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Primary Prediction Readout Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RaceSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, TelemetryCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ESTIMATED LAP TIME (ML ENSEMBLE)",
                        color = CockpitMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = LapTelemetry.formatLapTime(prediction.predictedLapTimeSeconds),
                        color = CockpitWhite,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format(Locale.US, "95%% Confidence: ± %.3fs uncertainty", prediction.uncertaintyMarginSeconds),
                        color = TelemetryCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Fuel Mass", color = CockpitSubtle, fontSize = 10.sp)
                            Text(String.format(Locale.US, "%.1f kg", prediction.fuelLoadKg), color = CockpitWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tire Wear Loss", color = CockpitSubtle, fontSize = 10.sp)
                            Text(String.format(Locale.US, "+%.3fs", prediction.tireDegradationSeconds), color = F1Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Fuel Advantage", color = CockpitSubtle, fontSize = 10.sp)
                            Text(String.format(Locale.US, "-%.3fs", prediction.fuelAdvantageSeconds), color = FlagGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Interactive Sliders & Configuration
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RaceSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "TELEMETRY FEATURE INPUTS",
                        color = CockpitWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compound Selection
                    Text("TIRE COMPOUND", color = CockpitMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(TireCompound.SOFT, TireCompound.MEDIUM, TireCompound.HARD).forEach { c ->
                            val isSelected = selectedCompound == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("select_compound_${c.code}")
                                    .background(
                                        if (isSelected) c.color.copy(alpha = 0.25f) else RaceSurfaceElevated,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(1.dp, if (isSelected) c.color else RaceSurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedCompound = c }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(c.color, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = c.code,
                                        color = CockpitWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lap Number Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RACE LAP NUMBER", color = CockpitMuted, fontSize = 11.sp)
                        Text("Lap $currentLap of $totalLaps", color = TelemetryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = currentLap.toFloat(),
                        onValueChange = { currentLap = it.toInt() },
                        valueRange = 1f..totalLaps.toFloat(),
                        colors = SliderDefaults.colors(thumbColor = TelemetryCyan, activeTrackColor = TelemetryCyan)
                    )

                    // Tire Age Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TIRE AGE ON STINT", color = CockpitMuted, fontSize = 11.sp)
                        Text("$tireAge Laps Old", color = TireMediumYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = tireAge.toFloat(),
                        onValueChange = { tireAge = it.toInt() },
                        valueRange = 1f..40f,
                        colors = SliderDefaults.colors(thumbColor = TireMediumYellow, activeTrackColor = TireMediumYellow)
                    )

                    // Track Temp Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TRACK TEMPERATURE", color = CockpitMuted, fontSize = 11.sp)
                        Text(String.format(Locale.US, "%.1f °C", trackTemp), color = F1Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = trackTemp,
                        onValueChange = { trackTemp = it },
                        valueRange = 20f..55f,
                        colors = SliderDefaults.colors(thumbColor = F1Red, activeTrackColor = F1Red)
                    )

                    // Dirty Air / Traffic Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RaceSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DIRTY AIR / TRAFFIC PENALTY", color = CockpitWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("+0.350s deficit from aerodynamic wash", color = CockpitSubtle, fontSize = 10.sp)
                        }
                        Switch(
                            checked = inTraffic,
                            onCheckedChange = { inTraffic = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = F1Red, checkedTrackColor = F1Red.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
