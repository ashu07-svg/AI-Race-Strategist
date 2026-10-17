package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RaceSession
import com.example.data.model.TireCompound
import com.example.ui.components.TireDegradationCurvesChart
import com.example.ui.theme.CockpitMuted
import com.example.ui.theme.CockpitSubtle
import com.example.ui.theme.CockpitWhite
import com.example.ui.theme.F1Red
import com.example.ui.theme.RaceSurface
import com.example.ui.theme.RaceSurfaceBorder
import com.example.ui.theme.RaceSurfaceElevated
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TireHardWhite
import com.example.ui.theme.TireMediumYellow
import com.example.ui.theme.TireSoftRed

@Composable
fun TireDegradationTab(
    session: RaceSession,
    modifier: Modifier = Modifier
) {
    val compounds = listOf(TireCompound.SOFT, TireCompound.MEDIUM, TireCompound.HARD)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            TireDegradationCurvesChart(compounds = compounds)
        }

        // Fuel correction and degradation physics card
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
                        text = "DEGRADATION MODELING PRINCIPLES",
                        color = CockpitWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Raw lap times do NOT equal true tire degradation. As a car races, fuel mass burns off at ~1.95 kg/lap (-0.064s/lap pace gain). This masks tire degradation until thermal wear accelerates nonlinearly past the compound 'cliff'. Our ML model isolates true tire wear by normalizing to zero-fuel baseline.",
                        color = CockpitMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Compound Specs Table
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
                        text = "PIRELLI COMPOUND CHARACTERISTICS",
                        color = CockpitWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    compounds.forEach { compound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RaceSurfaceElevated, RoundedCornerShape(8.dp))
                                .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(compound.color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = compound.fullName,
                                        color = CockpitWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Delta: +${compound.basePaceDeltaSeconds}s • Wear: +${compound.linearWearRate}s/lap",
                                        color = CockpitSubtle,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Cliff: Lap ${compound.cliffLap}",
                                    color = if (compound == TireCompound.SOFT) F1Red else TelemetryCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Max: ${compound.maxRecommendedLaps} Laps",
                                    color = CockpitMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // Crossover Points & Undercut Analysis
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
                        text = "COMPOUND CROSSOVER DELTAS",
                        color = CockpitWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Soft to Medium Crossover:", color = CockpitMuted, fontSize = 11.sp)
                        Text("Lap 16-18", color = TireMediumYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Medium to Hard Crossover:", color = CockpitMuted, fontSize = 11.sp)
                        Text("Lap 26-29", color = TireHardWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fresh Tire Out-Lap Undercut Gain:", color = CockpitMuted, fontSize = 11.sp)
                        Text("+1.420s", color = TelemetryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
