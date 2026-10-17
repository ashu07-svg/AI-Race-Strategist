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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ai.PitWallRecommendation
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

@Composable
fun PitWallRadioCard(
    recommendation: PitWallRecommendation?,
    isLoading: Boolean,
    onConsultAi: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf("") }
    var showRationale by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(RaceSurface, RoundedCornerShape(12.dp))
            .border(1.dp, F1Red.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header: Pit Wall Radio Banner
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
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Radio",
                            tint = CockpitWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PIT WALL TACTICAL RADIO",
                        color = CockpitWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini",
                        tint = TelemetryCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GEMINI 3.8 FLASH",
                        color = TelemetryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = TelemetryCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Analyzing telemetry & calculating undercut deltas...",
                        color = CockpitMuted,
                        fontSize = 12.sp
                    )
                }
            } else if (recommendation != null) {
                // Tactical Directive Pill
                Box(
                    modifier = Modifier
                        .background(
                            if (recommendation.tacticalAction.contains("BOX")) F1Red.copy(alpha = 0.2f)
                            else FlagGreen.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (recommendation.tacticalAction.contains("BOX")) F1Red else FlagGreen,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "DIRECTIVE: ${recommendation.tacticalAction}",
                        color = if (recommendation.tacticalAction.contains("BOX")) F1Red else FlagGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Radio Audio Transcript
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaceBackground, RoundedCornerShape(8.dp))
                        .border(1.dp, RaceSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "“${recommendation.radioTransmission}”",
                        color = CockpitWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Expandable Rationale Details
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showRationale) "Hide Telemetry Rationale ▲" else "View Strategy Rationale ▼",
                        color = TelemetryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .testTag("toggle_rationale_button")
                            .clickable { showRationale = !showRationale }
                    )
                    Text(
                        text = if (recommendation.isAiGenerated) "Online AI" else "Telemetry Heuristic",
                        color = CockpitSubtle,
                        fontSize = 10.sp
                    )
                }

                AnimatedVisibility(visible = showRationale) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(RaceSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = recommendation.fullStrategicRationale,
                            color = CockpitMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Pit Query input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = {
                        Text("Ask race strategist (e.g. 'Can we 1-stop on Hards?')", color = CockpitSubtle, fontSize = 11.sp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pit_wall_query_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CockpitWhite,
                        unfocusedTextColor = CockpitWhite,
                        focusedBorderColor = TelemetryCyan,
                        unfocusedBorderColor = RaceSurfaceBorder,
                        focusedContainerColor = RaceBackground,
                        unfocusedContainerColor = RaceBackground
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onConsultAi(queryText.ifBlank { null })
                        queryText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = F1Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("send_query_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = CockpitWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
