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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModelEvaluation
import com.example.ui.components.MetricCard
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
import java.util.Locale

@Composable
fun ModelEvaluationTab(
    modelEvaluation: ModelEvaluation = ModelEvaluation(),
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
            Text(
                text = "MACHINE LEARNING VALIDATION & METRICS",
                color = CockpitMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Metrics Grid: MAE, RMSE, R²
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Mean Absolute Error",
                    value = String.format(Locale.US, "%.3fs", modelEvaluation.maeSeconds),
                    subtitle = "Average lap error",
                    accentColor = FlagGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Root Mean Squared",
                    value = String.format(Locale.US, "%.3fs", modelEvaluation.rmseSeconds),
                    subtitle = "Penalizes outliers",
                    accentColor = TelemetryCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Coefficient R²",
                    value = String.format(Locale.US, "%.3f", modelEvaluation.rSquared),
                    subtitle = "93.2% Variance explained",
                    accentColor = TireMediumYellow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Dataset Size",
                    value = "${modelEvaluation.sampleCount}",
                    subtitle = "Telemetry laps evaluated",
                    accentColor = CockpitWhite,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Feature Importance Ranking
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
                        text = "FEATURE IMPORTANCE (SHAP / GINI IMPORTANCE)",
                        color = CockpitWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    modelEvaluation.featureImportances.forEach { feature ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = feature.featureName,
                                    color = CockpitWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", feature.importanceScore * 100),
                                    color = TelemetryCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { feature.importanceScore },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = if (feature.importanceScore > 0.25f) F1Red else TelemetryCyan,
                                trackColor = RaceSurfaceElevated,
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = feature.description,
                                color = CockpitSubtle,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // Cross-Validation & Target Leakage Prevention Card
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
                        text = "LEAKAGE-FREE CHRONOLOGICAL VALIDATION",
                        color = CockpitWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Standard random k-fold cross-validation is invalid in motorsport telemetry due to high temporal autocorrelation between successive laps. Our pipeline enforces strict race-held-out validation, ensuring training and test splits belong to entirely distinct Grand Prix sessions to guarantee generalized real-world predictive power.",
                        color = CockpitMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
