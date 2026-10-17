package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LapTelemetry
import com.example.data.model.StrategyResult
import com.example.data.model.TireCompound
import com.example.ui.theme.CockpitMuted
import com.example.ui.theme.CockpitSubtle
import com.example.ui.theme.CockpitWhite
import com.example.ui.theme.F1Red
import com.example.ui.theme.FlagSafetyCar
import com.example.ui.theme.RaceSurface
import com.example.ui.theme.RaceSurfaceBorder
import com.example.ui.theme.RaceSurfaceElevated
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TireHardWhite
import com.example.ui.theme.TireMediumYellow
import com.example.ui.theme.TireSoftRed
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun LapPaceChart(
    laps: List<LapTelemetry>,
    showFuelCorrected: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (laps.isEmpty()) return

    val validLaps = laps.filter { it.isValidPace }
    val minPace = (validLaps.minOfOrNull { it.lapTimeSeconds } ?: 80f) - 1.0f
    val maxPace = (validLaps.maxOfOrNull { it.lapTimeSeconds } ?: 90f) + 1.5f
    val totalLaps = laps.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RaceSurface, RoundedCornerShape(12.dp))
            .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LAP-BY-LAP PACE & DEGRADATION",
                color = CockpitWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(F1Red, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Raw Pace", color = CockpitMuted, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(TelemetryCyan, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Fuel-Corrected", color = CockpitMuted, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val w = size.width
            val h = size.height
            val padLeft = 40f
            val padBottom = 30f
            val graphW = w - padLeft - 10f
            val graphH = h - padBottom - 10f

            // Grid lines
            val ySteps = 4
            for (i in 0..ySteps) {
                val yVal = minPace + (maxPace - minPace) * (i.toFloat() / ySteps)
                val y = 10f + graphH * (1f - (yVal - minPace) / (maxPace - minPace))
                drawLine(
                    color = RaceSurfaceBorder.copy(alpha = 0.5f),
                    start = Offset(padLeft, y),
                    end = Offset(w - 10f, y),
                    strokeWidth = 1f
                )
            }

            // Raw pace path
            val rawPath = Path()
            val fuelCorrectedPath = Path()

            var startedRaw = false
            var startedFc = false

            laps.forEach { lap ->
                val x = padLeft + ((lap.lapNumber.toFloat() - 1f) / (totalLaps - 1f)) * graphW
                val clampedLapTime = min(maxPace, max(minPace, lap.lapTimeSeconds))
                val yRaw = 10f + graphH * (1f - (clampedLapTime - minPace) / (maxPace - minPace))

                if (lap.isSafetyCar) {
                    // Draw safety car band
                    drawRect(
                        color = FlagSafetyCar.copy(alpha = 0.15f),
                        topLeft = Offset(x - 2f, 10f),
                        size = Size(graphW / totalLaps, graphH)
                    )
                }

                if (lap.isValidPace) {
                    if (!startedRaw) {
                        rawPath.moveTo(x, yRaw)
                        startedRaw = true
                    } else {
                        rawPath.lineTo(x, yRaw)
                    }

                    if (showFuelCorrected) {
                        val clampedFc = min(maxPace, max(minPace, lap.fuelCorrectedLapTimeSeconds))
                        val yFc = 10f + graphH * (1f - (clampedFc - minPace) / (maxPace - minPace))
                        if (!startedFc) {
                            fuelCorrectedPath.moveTo(x, yFc)
                            startedFc = true
                        } else {
                            fuelCorrectedPath.lineTo(x, yFc)
                        }
                    }
                } else if (lap.isPitIn || lap.isPitOut) {
                    // Mark pit stop with yellow tick
                    drawCircle(
                        color = TireMediumYellow,
                        radius = 4f,
                        center = Offset(x, yRaw)
                    )
                }
            }

            // Draw fuel-corrected dashed line
            if (showFuelCorrected) {
                drawPath(
                    path = fuelCorrectedPath,
                    color = TelemetryCyan,
                    style = Stroke(
                        width = 2.5f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )
            }

            // Draw raw pace line
            drawPath(
                path = rawPath,
                color = F1Red,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun TireDegradationCurvesChart(
    compounds: List<TireCompound> = listOf(TireCompound.SOFT, TireCompound.MEDIUM, TireCompound.HARD),
    maxLaps: Int = 38,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RaceSurface, RoundedCornerShape(12.dp))
            .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIRE DEGRADATION (PACE LOSS VS AGE)",
                color = CockpitWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                compounds.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(c.color, CircleShape))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(c.code, color = CockpitMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            val w = size.width
            val h = size.height
            val padLeft = 35f
            val padBottom = 25f
            val graphW = w - padLeft - 10f
            val graphH = h - padBottom - 10f

            val maxDeltaSeconds = 3.5f

            // Grid lines
            for (i in 0..4) {
                val ySec = (maxDeltaSeconds / 4) * i
                val y = 10f + graphH * (1f - (ySec / maxDeltaSeconds))
                drawLine(
                    color = RaceSurfaceBorder.copy(alpha = 0.4f),
                    start = Offset(padLeft, y),
                    end = Offset(w - 10f, y),
                    strokeWidth = 1f
                )
            }

            // Draw curves for each compound
            compounds.forEach { compound ->
                val path = Path()
                for (age in 1..maxLaps) {
                    val deg = compound.getDegradation(age) + compound.basePaceDeltaSeconds
                    val clampedDeg = min(maxDeltaSeconds, deg)
                    val x = padLeft + (age.toFloat() / maxLaps) * graphW
                    val y = 10f + graphH * (1f - (clampedDeg / maxDeltaSeconds))

                    if (age == 1) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Render observed telemetry data points (simulated observed scatter dots)
                    if (age % 3 == 0 && age <= compound.maxRecommendedLaps) {
                        val scatterNoise = ((age * 37) % 7 - 3) * 0.025f
                        val yDot = 10f + graphH * (1f - (min(maxDeltaSeconds, deg + scatterNoise) / maxDeltaSeconds))
                        drawCircle(
                            color = compound.color.copy(alpha = 0.7f),
                            radius = 3.5f,
                            center = Offset(x, yDot)
                        )
                    }
                }

                // Fitted model line
                drawPath(
                    path = path,
                    color = compound.color,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Cliff lap marker
                val cliffX = padLeft + (compound.cliffLap.toFloat() / maxLaps) * graphW
                if (cliffX < w - 10f) {
                    drawLine(
                        color = compound.color.copy(alpha = 0.35f),
                        start = Offset(cliffX, 10f),
                        end = Offset(cliffX, h - padBottom),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Lap 1 (New)", color = CockpitSubtle, fontSize = 9.sp)
            Text("Dotted Points: Observed Telemetry • Solid: ML Polynomial Fit", color = CockpitSubtle, fontSize = 9.sp)
            Text("Lap $maxLaps", color = CockpitSubtle, fontSize = 9.sp)
        }
    }
}

@Composable
fun StintTimelineBar(
    strategy: StrategyResult,
    totalLaps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(RaceSurfaceElevated, RoundedCornerShape(6.dp))
                .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(6.dp))
                .padding(2.dp)
        ) {
            strategy.stints.forEach { stint ->
                val fraction = stint.laps.toFloat() / totalLaps
                Box(
                    modifier = Modifier
                        .weight(fraction)
                        .height(22.dp)
                        .background(stint.compound.color, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${stint.compound.code} (${stint.laps}L)",
                        color = if (stint.compound == TireCompound.HARD) Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
