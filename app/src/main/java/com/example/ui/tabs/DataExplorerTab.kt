package com.example.ui.tabs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LapTelemetry
import com.example.data.model.RaceSession
import com.example.data.model.TireCompound
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
import com.example.ui.theme.TireMediumYellow
import java.util.Locale

@Composable
fun DataExplorerTab(
    session: RaceSession,
    laps: List<LapTelemetry>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilterCompound by remember { mutableStateOf<TireCompound?>(null) }

    val filteredLaps = if (selectedFilterCompound != null) {
        laps.filter { it.compound == selectedFilterCompound }
    } else {
        laps
    }

    val fastestLap = laps.filter { it.isValidPace }.minByOrNull { it.lapTimeSeconds }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Telemetry Overview & Export Header
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
                        Column {
                            Text(
                                text = "SESSION TELEMETRY EXPLORER",
                                color = CockpitWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${laps.size} Laps Logged • Fastest: ${fastestLap?.formattedLapTime ?: "--"}",
                                color = TelemetryCyan,
                                fontSize = 11.sp
                            )
                        }

                        // Export CSV Action
                        Button(
                            onClick = {
                                val csv = RaceDataRepository.exportTelemetryCsv(session, laps)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Telemetry CSV", csv)
                                clipboard.setPrimaryClip(clip)

                                // Trigger Share Intent
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/csv"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Export Telemetry CSV")
                                context.startActivity(shareIntent)

                                Toast.makeText(context, "Telemetry CSV Copied & Ready to Export!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = F1Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("export_csv_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Export", tint = CockpitWhite, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXPORT CSV", color = CockpitWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compound Filter Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedFilterCompound == null) TelemetryCyan else RaceSurfaceElevated,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, if (selectedFilterCompound == null) TelemetryCyan else RaceSurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedFilterCompound = null }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ALL (${laps.size})",
                                color = if (selectedFilterCompound == null) RaceBackground else CockpitWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        listOf(TireCompound.SOFT, TireCompound.MEDIUM, TireCompound.HARD).forEach { c ->
                            val isSelected = selectedFilterCompound == c
                            val count = laps.count { it.compound == c }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) c.color else RaceSurfaceElevated,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(1.dp, if (isSelected) c.color else RaceSurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedFilterCompound = c }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${c.code} ($count)",
                                    color = if (isSelected && c == TireCompound.HARD) Color.Black else CockpitWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Telemetry Data Table Items
        items(filteredLaps) { lap ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RaceSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "L${lap.lapNumber}",
                                color = CockpitMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(36.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(lap.compound.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lap.compound.code,
                                    color = if (lap.compound == TireCompound.HARD) Color.Black else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lap.formattedLapTime,
                                color = if (lap.lapNumber == fastestLap?.lapNumber) TelemetryCyan else CockpitWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Status Note or Speed
                        if (lap.note.isNotBlank()) {
                            Text(
                                text = lap.note,
                                color = if (lap.isSafetyCar) FlagSafetyCar else TireMediumYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Text(
                                text = String.format(Locale.US, "%.0f km/h", lap.telemetrySpeedTrapKph),
                                color = CockpitSubtle,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sector times & tire age & fuel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format(Locale.US, "S1: %.2f  S2: %.2f  S3: %.2f", lap.sector1Seconds, lap.sector2Seconds, lap.sector3Seconds),
                            color = CockpitSubtle,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Age: ${lap.tireAge}L • Fuel: ${String.format(Locale.US, "%.0f", lap.fuelLoadKg)}kg",
                            color = CockpitMuted,
                            fontSize = 10.sp
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
