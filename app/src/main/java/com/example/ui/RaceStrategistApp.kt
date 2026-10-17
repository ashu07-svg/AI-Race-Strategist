package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.RaceDataRepository
import com.example.ui.components.MotorsportHeader
import com.example.ui.tabs.DataExplorerTab
import com.example.ui.tabs.LapPredictionTab
import com.example.ui.tabs.ModelEvaluationTab
import com.example.ui.tabs.OverviewTab
import com.example.ui.tabs.StrategyOptimizerTab
import com.example.ui.tabs.TireDegradationTab
import com.example.ui.theme.CockpitMuted
import com.example.ui.theme.CockpitSubtle
import com.example.ui.theme.CockpitWhite
import com.example.ui.theme.F1Red
import com.example.ui.theme.RaceBackground
import com.example.ui.theme.RaceSurface
import com.example.ui.theme.RaceSurfaceBorder
import com.example.ui.theme.RaceSurfaceElevated
import com.example.ui.theme.TelemetryCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceStrategistApp(
    viewModel: RaceStrategistViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDriverPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Back button handling: return to Overview if in other tabs
    if (uiState.activeTab != AppTab.OVERVIEW) {
        BackHandler {
            viewModel.switchTab(AppTab.OVERVIEW)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = RaceBackground,
        bottomBar = {
            NavigationBar(
                containerColor = RaceSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(width = 1.dp, color = RaceSurfaceBorder)
                    .testTag("main_navigation_bar")
            ) {
                val navItems = listOf(
                    Triple(AppTab.OVERVIEW, "Overview", Icons.Default.Dashboard),
                    Triple(AppTab.STRATEGY, "Strategy", Icons.Default.Timeline),
                    Triple(AppTab.TIRES, "Tires", Icons.Default.DonutLarge),
                    Triple(AppTab.ML_PREDICT, "Predict", Icons.Default.Tune),
                    Triple(AppTab.EVALUATION, "Model", Icons.Default.Analytics),
                    Triple(AppTab.DATA, "Data", Icons.Default.TableRows)
                )

                navItems.forEach { (tab, label, icon) ->
                    val isSelected = uiState.activeTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.switchTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CockpitWhite,
                            selectedTextColor = TelemetryCyan,
                            unselectedIconColor = CockpitSubtle,
                            unselectedTextColor = CockpitMuted,
                            indicatorColor = F1Red
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with telemetry stats and safety car switch
            MotorsportHeader(
                session = uiState.session,
                isSafetyCarActive = uiState.isSafetyCarActive,
                onToggleSafetyCar = { viewModel.toggleSafetyCar() },
                onToggleDemoMode = { viewModel.toggleDemoMode() }
            )

            // Driver Selector Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RaceSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(uiState.session.driver.teamColorHex), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DRIVER: ${uiState.session.driver.name.uppercase()}",
                        color = CockpitWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "CHANGE DRIVER ▾",
                    color = TelemetryCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .testTag("change_driver_button")
                        .clickable { showDriverPicker = true }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Tab Content Router
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.activeTab) {
                    AppTab.OVERVIEW -> OverviewTab(
                        session = uiState.session,
                        laps = uiState.laps,
                        strategies = uiState.strategies,
                        pitWallAi = uiState.pitWallRecommendation,
                        isAiLoading = uiState.isAiLoading,
                        onConsultAi = { viewModel.consultAi(it) },
                        onSelectCircuit = { viewModel.selectCircuit(it) },
                        onSelectDriver = { viewModel.selectDriver(it) }
                    )

                    AppTab.STRATEGY -> StrategyOptimizerTab(
                        session = uiState.session,
                        config = uiState.config,
                        strategies = uiState.strategies,
                        onUpdatePitLoss = { viewModel.updatePitLoss(it) },
                        onUpdateWeather = { viewModel.updateWeather(it) },
                        onToggleSafetyCar = { viewModel.toggleSafetyCar() }
                    )

                    AppTab.TIRES -> TireDegradationTab(
                        session = uiState.session
                    )

                    AppTab.ML_PREDICT -> LapPredictionTab(
                        session = uiState.session
                    )

                    AppTab.EVALUATION -> ModelEvaluationTab(
                        modelEvaluation = uiState.modelEvaluation
                    )

                    AppTab.DATA -> DataExplorerTab(
                        session = uiState.session,
                        laps = uiState.laps
                    )
                }
            }
        }
    }

    // Driver Selection Bottom Sheet
    if (showDriverPicker) {
        ModalBottomSheet(
            onDismissRequest = { showDriverPicker = false },
            sheetState = sheetState,
            containerColor = RaceSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "SELECT DRIVER TELEMETRY",
                    color = CockpitWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))

                RaceDataRepository.drivers.forEach { driver ->
                    val isSelected = driver.code == uiState.session.driver.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) RaceSurfaceElevated else RaceBackground,
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isSelected) F1Red else RaceSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.selectDriver(driver.code)
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showDriverPicker = false
                                }
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(driver.teamColorHex), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "#${driver.number} ${driver.name}",
                                    color = CockpitWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = driver.team,
                                    color = CockpitMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isSelected) {
                            Text("ACTIVE", color = TelemetryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
