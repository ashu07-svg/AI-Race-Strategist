"""
AI Race Strategist - Motorsport Race Control & Telemetry API Backend.
Powered by FastAPI, FastF1, scikit-learn, and NumPy.
"""

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict, Any

from data_loader import F1DataLoader
from model_trainer import LapTimeModelTrainer
from strategy_optimizer import StrategyOptimizer

app = FastAPI(
    title="AI Race Strategist API",
    description="F1 Telemetry Ingestion, ML Lap-Time Prediction, and Strategy Simulation Engine",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

data_loader = F1DataLoader()
model_trainer = LapTimeModelTrainer()
strategy_optimizer = StrategyOptimizer()


class PredictionRequest(BaseModel):
    current_lap: int = 15
    total_laps: int = 53
    compound: str = "MEDIUM"
    tire_age: int = 15
    track_temp_c: float = 35.0
    in_traffic: bool = False


class SimulationRequest(BaseModel):
    total_laps: int = 53
    base_lap_seconds: float = 81.250
    pit_lane_loss_seconds: float = 23.5
    safety_car_pit_loss_seconds: float = 12.0
    safety_car_lap: Optional[int] = None


@app.get("/")
def root():
    return {
        "service": "AI Race Strategist Backend",
        "status": "Online",
        "documentation": "/docs",
        "endpoints": ["/api/sessions", "/api/session-data", "/api/train-model", "/api/predict-lap", "/api/optimize-strategy"]
    }


@app.get("/api/health")
def health():
    return {"status": "healthy"}


@app.get("/api/sessions")
def get_supported_sessions():
    return {
        "championship": "Formula 1",
        "supported_circuits": [
            {"id": "monza", "name": "Monza (Italian GP)", "total_laps": 53, "pit_loss": 23.5},
            {"id": "silverstone", "name": "Silverstone (British GP)", "total_laps": 52, "pit_loss": 21.8},
            {"id": "spa", "name": "Spa-Francorchamps (Belgian GP)", "total_laps": 44, "pit_loss": 22.0},
            {"id": "bahrain", "name": "Bahrain (Sakhir GP)", "total_laps": 57, "pit_loss": 22.5},
            {"id": "monaco", "name": "Monaco GP", "total_laps": 78, "pit_loss": 24.5},
            {"id": "suzuka", "name": "Suzuka (Japanese GP)", "total_laps": 53, "pit_loss": 22.4}
        ],
        "default_driver": "VER"
    }


@app.get("/api/session-data")
def get_session_data(
    year: int = Query(2024),
    gp: str = Query("Monza"),
    driver: str = Query("VER"),
    demo_mode: bool = Query(False)
):
    df, meta = data_loader.load_session_laps(
        year=year,
        gp=gp,
        driver_code=driver,
        force_synthetic=demo_mode
    )
    records = df.to_dict(orient="records")
    return {
        "metadata": meta,
        "laps_count": len(records),
        "laps": records[:60]
    }


@app.post("/api/train-model")
def train_model(year: int = 2024, gp: str = "Monza", driver: str = "VER"):
    df, meta = data_loader.load_session_laps(year=year, gp=gp, driver_code=driver)
    eval_metrics = model_trainer.train_and_evaluate(df)
    return {
        "status": "success",
        "dataset": meta,
        "metrics": eval_metrics
    }


@app.post("/api/predict-lap")
def predict_lap(req: PredictionRequest):
    comp = req.compound.upper()
    comp_delta = {"SOFT": 0.0, "MEDIUM": 0.42, "HARD": 0.92}.get(comp, 0.5)
    wear_rate = {"SOFT": 0.095, "MEDIUM": 0.055, "HARD": 0.032}.get(comp, 0.05)

    base = 81.250 + comp_delta
    wear = wear_rate * req.tire_age
    fuel_load = max(0, req.total_laps - req.current_lap) * 1.95
    fuel_adv = (req.total_laps * 1.95 - fuel_load) * 0.033
    traffic = 0.35 if req.in_traffic else 0.0

    predicted = base + wear - fuel_adv + traffic
    return {
        "predicted_lap_time_seconds": round(predicted, 3),
        "fuel_load_kg": round(fuel_load, 1),
        "tire_degradation_seconds": round(wear, 3),
        "fuel_advantage_seconds": round(fuel_adv, 3),
        "uncertainty_margin_seconds": 0.18
    }


@app.post("/api/optimize-strategy")
def optimize_strategy(req: SimulationRequest):
    results = strategy_optimizer.simulate_strategies(
        total_laps=req.total_laps,
        base_lap=req.base_lap_seconds,
        pit_lane_loss=req.pit_lane_loss_seconds,
        sc_pit_loss=req.safety_car_pit_loss_seconds,
        safety_car_lap=req.safety_car_lap
    )
    return {
        "optimal_plan": results[0]["plan_name"],
        "strategies_ranked": results
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
