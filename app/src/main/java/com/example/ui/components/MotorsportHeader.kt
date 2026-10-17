package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
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
import com.example.data.model.RaceSession
import com.example.ui.theme.CockpitMuted
import com.example.ui.theme.CockpitWhite
import com.example.ui.theme.F1Red
import com.example.ui.theme.FlagGreen
import com.example.ui.theme.FlagSafetyCar
import com.example.ui.theme.RaceSurface
import com.example.ui.theme.RaceSurfaceBorder
import com.example.ui.theme.TelemetryCyan

@Composable
fun MotorsportHeader(
    session: RaceSession,
    isSafetyCarActive: Boolean,
    onToggleSafetyCar: () -> Unit,
    onToggleDemoMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(RaceSurface)
            .border(width = 1.dp, color = RaceSurfaceBorder)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            // Top Row: Title, Data Source Status, and Demo Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isSafetyCarActive) FlagSafetyCar else FlagGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI RACE STRATEGIST",
                        color = CockpitWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                // Data provenance badge
                Row(
                    modifier = Modifier
                        .testTag("mode_toggle_badge")
                        .background(
                            if (session.isSynthetic) Color(0xFF2E2211) else Color(0xFF10281F),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (session.isSynthetic) FlagSafetyCar.copy(alpha = 0.5f) else FlagGreen.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onToggleDemoMode() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (session.isSynthetic) "DEMO (SYNTHETIC)" else "FASTF1 ARCHIVE",
                        color = if (session.isSynthetic) FlagSafetyCar else FlagGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Circuit, Driver, Weather telemetry strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${session.circuit.name.uppercase()} • ${session.circuit.totalLaps} LAPS",
                        color = CockpitWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "#${session.driver.number} ${session.driver.name} (${session.driver.team})",
                        color = TelemetryCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Safety Car deployment interactive trigger
                Row(
                    modifier = Modifier
                        .testTag("safety_car_toggle")
                        .background(
                            if (isSafetyCarActive) FlagSafetyCar.copy(alpha = 0.25f) else RaceSurfaceBorder.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSafetyCarActive) FlagSafetyCar else RaceSurfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleSafetyCar() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSafetyCarActive) Icons.Default.Warning else Icons.Default.Flag,
                        contentDescription = "Safety Car",
                        tint = if (isSafetyCarActive) FlagSafetyCar else CockpitMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSafetyCarActive) "SC ACTIVE (LAP 24)" else "SC STANDBY",
                        color = if (isSafetyCarActive) FlagSafetyCar else CockpitMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
