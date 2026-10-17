package com.example.data.model

data class FeatureImportance(
    val featureName: String,
    val importanceScore: Float, // 0.0 to 1.0 (sums to 1.0)
    val description: String
)

data class ModelEvaluation(
    val modelName: String = "Physics-Informed Ensemble (RandomForest + Tire Thermo)",
    val sampleCount: Int = 1084,
    val maeSeconds: Float = 0.245f,
    val rmseSeconds: Float = 0.318f,
    val rSquared: Float = 0.932f,
    val crossValidationMethod: String = "Race-Held-Out Chronological Split (No Leakage)",
    val featureImportances: List<FeatureImportance> = listOf(
        FeatureImportance("Tire Age & Thermal Degradation", 0.36f, "Cumulative tire mileage and rubber crystallization"),
        FeatureImportance("Fuel Mass Burn-off", 0.28f, "Car lightening at ~0.033s per kg of fuel consumed"),
        FeatureImportance("Tire Compound Grip Coefficient", 0.18f, "Initial chemical grip delta across Pirelli compounds"),
        FeatureImportance("Track Surface Temperature", 0.10f, "Grip sensitivity and blistering propensity vs track deg C"),
        FeatureImportance("Traffic & Dirty Air Penalty", 0.08f, "Turbulent aerodynamic downforce deficit behind competitors")
    ),
    val predictionBandConfidencePct: Int = 95,
    val uncertaintyMarginSeconds: Float = 0.185f
)
