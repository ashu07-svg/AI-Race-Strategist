package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GrandPrixCircuit
import com.example.data.model.RaceSession
import com.example.data.repository.RaceDataRepository
import com.example.ui.theme.CockpitMuted
import com.example.ui.theme.CockpitSubtle
import com.example.ui.theme.CockpitWhite
import com.example.ui.theme.F1Red
import com.example.ui.theme.FlagGreen
import com.example.ui.theme.FlagSafetyCar
import com.example.ui.theme.RaceBackground
import com.example.ui.theme.RaceSurface
import com.example.ui.theme.RaceSurfaceBorder
import com.example.ui.theme.RaceSurfaceElevated
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TireHardWhite
import com.example.ui.theme.TireMediumYellow
import java.util.Locale

@Composable
fun RaceSessionSelector(
    currentSession: RaceSession,
    onSessionSelected: (championship: String, season: Int, circuitId: String, sessionType: String, isSynthetic: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    var selectedChampionship by remember(currentSession) { mutableStateOf(currentSession.championship) }
    var selectedSeason by remember(currentSession) { mutableIntStateOf(currentSession.season) }
    var selectedCircuitId by remember(currentSession) { mutableStateOf(currentSession.circuit.id) }
    var selectedSessionType by remember(currentSession) { mutableStateOf(currentSession.sessionType) }
    var isSynthetic by remember(currentSession) { mutableStateOf(currentSession.isSynthetic) }

    val activeCircuit = RaceDataRepository.circuits.firstOrNull { it.id == selectedCircuitId }
        ?: RaceDataRepository.circuits.first()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RaceSurface, RoundedCornerShape(16.dp))
            .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("race_session_selector")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(F1Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Session Selector",
                        tint = CockpitWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "RACE SESSION SELECTOR",
                        color = CockpitWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Configure championship, event, and telemetry provenance",
                        color = CockpitMuted,
                        fontSize = 10.sp
                    )
                }
            }

            if (onDismiss != null) {
                Text(
                    text = "CLOSE ✕",
                    color = CockpitMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .testTag("close_session_selector")
                        .clickable { onDismiss() }
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // DATA SOURCE STATUS INDICATOR CARD (REAL VS SYNTHETIC)
        DataSourceStatusCard(
            isSynthetic = isSynthetic,
            onToggleSynthetic = { isSynthetic = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. CHAMPIONSHIP SELECTOR
        Text(
            text = "CHAMPIONSHIP",
            color = CockpitMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RaceDataRepository.championships.forEach { champ ->
                val isSelected = champ == selectedChampionship
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("champ_select_${champ.replace(" ", "_").lowercase()}")
                        .background(
                            if (isSelected) F1Red else RaceSurfaceElevated,
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, if (isSelected) F1Red else RaceSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedChampionship = champ }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = champ,
                        color = CockpitWhite,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. SEASON & SESSION TYPE SELECTORS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Season Selector
            Column(modifier = Modifier.weight(0.42f)) {
                Text(
                    text = "SEASON",
                    color = CockpitMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RaceDataRepository.seasons.forEach { yr ->
                        val isSelected = yr == selectedSeason
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("season_select_$yr")
                            .background(
                                    if (isSelected) TelemetryCyan else RaceSurfaceElevated,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, if (isSelected) TelemetryCyan else RaceSurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedSeason = yr }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = yr.toString(),
                                color = if (isSelected) RaceBackground else CockpitWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Session Type Selector
            Column(modifier = Modifier.weight(0.58f)) {
                Text(
                    text = "SESSION TYPE",
                    color = CockpitMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(RaceDataRepository.sessionTypes) { sType ->
                        val isSelected = sType == selectedSessionType
                        val shortLabel = sType.substringBefore(" (")
                        Box(
                            modifier = Modifier
                                .testTag("session_type_${shortLabel.lowercase()}")
                                .background(
                                    if (isSelected) TelemetryCyan else RaceSurfaceElevated,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, if (isSelected) TelemetryCyan else RaceSurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedSessionType = sType }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = shortLabel,
                                color = if (isSelected) RaceBackground else CockpitWhite,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. GRAND PRIX EVENT SELECTOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GRAND PRIX EVENT / CIRCUIT",
                color = CockpitMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "${RaceDataRepository.circuits.size} Venues Available",
                color = CockpitSubtle,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Carousel of Circuit Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(RaceDataRepository.circuits) { circuit ->
                val isSelected = circuit.id == selectedCircuitId
                CircuitCard(
                    circuit = circuit,
                    isSelected = isSelected,
                    onClick = { selectedCircuitId = circuit.id }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. SELECTED EVENT SUMMARY & TELEMETRY PROFILE
        SelectedEventDetailsCard(
            circuit = activeCircuit,
            season = selectedSeason,
            sessionType = selectedSessionType,
            isSynthetic = isSynthetic
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. CONFIRM & LOAD SESSION ACTION
        Button(
            onClick = {
                onSessionSelected(
                    selectedChampionship,
                    selectedSeason,
                    selectedCircuitId,
                    selectedSessionType,
                    isSynthetic
                )
                onDismiss?.invoke()
            },
            colors = ButtonDefaults.buttonColors(containerColor = F1Red),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("apply_session_button")
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = CockpitWhite,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "APPLY & LOAD SESSION TELEMETRY",
                color = CockpitWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Data Source Status Card displaying whether telemetry is Real (FastF1 Synced) or Synthetic (ML Simulation).
 */
@Composable
private fun DataSourceStatusCard(
    isSynthetic: Boolean,
    onToggleSynthetic: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSynthetic) Color(0xFF261C0D) else Color(0xFF0F261B),
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (isSynthetic) FlagSafetyCar else FlagGreen,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
            .testTag("data_source_status_card")
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
                            .size(10.dp)
                            .background(if (isSynthetic) FlagSafetyCar else FlagGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSynthetic) "DATA SOURCE: SYNTHETIC SIMULATION" else "DATA SOURCE: REAL HISTORICAL TELEMETRY",
                        color = if (isSynthetic) FlagSafetyCar else FlagGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Toggle switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isSynthetic) "SYNTHETIC" else "FASTF1 REAL",
                        color = CockpitWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = !isSynthetic,
                        onCheckedChange = { onToggleSynthetic(!it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FlagGreen,
                            checkedTrackColor = FlagGreen.copy(alpha = 0.5f),
                            uncheckedThumbColor = FlagSafetyCar,
                            uncheckedTrackColor = FlagSafetyCar.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .size(38.dp, 24.dp)
                            .testTag("synthetic_real_toggle")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (!isSynthetic) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced",
                        tint = FlagGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FastF1 Archive Synced • 100 Hz GPS & FIA Official Timing • Local Cache Active",
                        color = CockpitWhite,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Synthetic",
                        tint = FlagSafetyCar,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Physics-Calibrated Synthetic Engine: Used for offline setup modeling. Clearly distinguished from real FIA timing records.",
                        color = CockpitWhite,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Visual card for individual circuits in the selector.
 */
@Composable
private fun CircuitCard(
    circuit: GrandPrixCircuit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .testTag("circuit_card_${circuit.id}")
            .background(
                if (isSelected) RaceSurfaceElevated else RaceBackground,
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (isSelected) F1Red else RaceSurfaceBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(if (isSelected) F1Red else RaceSurfaceBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "RD ${circuit.roundNumber} • ${circuit.countryCode}",
                        color = CockpitWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(TelemetryCyan, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = circuit.name.substringBefore(" ("),
                color = CockpitWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = circuit.location,
                color = CockpitSubtle,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${circuit.totalLaps} Laps",
                    color = TelemetryCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${circuit.tireDegradationSeverity} Deg",
                    color = if (circuit.tireDegradationSeverity == "High") F1Red else TireMediumYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Session telemetry metadata breakdown for the selected circuit.
 */
@Composable
private fun SelectedEventDetailsCard(
    circuit: GrandPrixCircuit,
    season: Int,
    sessionType: String,
    isSynthetic: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(RaceBackground, RoundedCornerShape(10.dp))
            .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$season ${circuit.name.uppercase()} • $sessionType",
                    color = CockpitWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${circuit.circuitLengthKm} km Track",
                    color = TelemetryCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Baseline Pole Pace", color = CockpitSubtle, fontSize = 9.sp)
                    Text(
                        String.format(Locale.US, "%.3fs", circuit.baseQualifyingLapTime),
                        color = CockpitWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text("Pit Lane Loss", color = CockpitSubtle, fontSize = 9.sp)
                    Text(
                        String.format(Locale.US, "%.1fs (SC: %.1fs)", circuit.averagePitLaneLossSeconds, circuit.safetyCarPitLossSeconds),
                        color = CockpitWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text("Overtake Difficulty", color = CockpitSubtle, fontSize = 9.sp)
                    Text(
                        circuit.overtakingDifficulty,
                        color = if (circuit.overtakingDifficulty == "High") F1Red else FlagGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pirelli Allocation: ${circuit.tireAllocation}",
                    color = TireMediumYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Temp: ${circuit.defaultWeather.trackTempC}°C Track",
                    color = CockpitMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
