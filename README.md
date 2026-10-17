# AI Race Strategist

AI-powered motorsport race strategy prediction system for Formula 1 and circuit racing championships. Features lap-time modeling, nonlinear tire degradation analytics, multi-stop pit strategy optimization, and live pit wall AI race engineer briefings powered by Gemini 3.8 Flash.

---

## Architecture & System Overview

AI Race Strategist provides a dual-runtime motorsport telemetry environment:

1. **Android Telemetry Engineering Dashboard (Live in AI Studio Emulator)**
   - Native Kotlin & Jetpack Compose implementation.
   - Built to professional race control aesthetic: dark charcoal UI (`#0C0F14`), telemetry cyan (`#00F0FF`), Pirelli tire compound identifiers, and high-contrast telemetry charts.
   - Features:
     - **Session Selector**: Real Grand Prix circuits (Monza, Silverstone, Spa-Francorchamps, Bahrain, Monaco, Suzuka) and drivers (Verstappen, Norris, Leclerc, Hamilton, Piastri, etc.).
     - **Tire Degradation Analytics**: Interactive Canvas curves for Soft, Medium, and Hard tires, showing observed points, model polynomial fits, and degradation cliff laps.
     - **Strategy Optimizer**: Real-time simulation of 1-stop, 2-stop, and 3-stop permutations with pit loss modeling (normal green flag vs cheap Safety Car stops), ranked by predicted total race time down to milliseconds.
     - **Lap-Time Predictor**: Interactive ML regression sliders factoring in tire age, fuel mass burn-off (-0.033s/kg), track temperature sensitivity, and dirty air penalties.
     - **Pit Wall AI Briefing**: Tactical radio engineer calls and stint directives powered by `gemini-3.8-flash`.
     - **Data Explorer & CSV Export**: Complete lap-by-lap telemetry logs with one-tap CSV export and system sharing.

2. **Python FastAPI Backend & FastF1 Data Pipeline (`backend/`)**
   - Self-contained runnable Python service for local development and historical F1 data caching.
   - Endpoints for session discovery, data cleaning, ML model training, lap-time prediction, and strategy simulation.

---

## Running the Android App in AI Studio

The Android app is fully configured and runs directly in the AI Studio cloud emulator:
- Simply launch or refresh the streaming emulator to view the live dashboard.
- Tap **Change Driver** to switch drivers and team telemetry.
- Tap **SC Standby / SC Active** to simulate an immediate Safety Car deployment and watch the strategy optimizer recalculate the optimal pit window.
- Tap **Demo (Synthetic)** to toggle between live FastF1 historical data and calibrated offline simulation.
- Tap **Export CSV** in the Data tab to export full session telemetry.

---

## Running the Python Backend Locally (Windows & VS Code)

If you would like to run the Python backend service locally on Windows or within Visual Studio Code:

### 1. Prerequisites
- Python 3.10, 3.11, or 3.12 installed on Windows.
- [Visual Studio Code](https://code.visualstudio.com/) with the Python extension.

### 2. Setup Virtual Environment in Windows PowerShell

Open Windows PowerShell or the VS Code Terminal (`Ctrl + \``) in the project root:

```powershell
# Navigate to the backend directory
cd backend

# Create a virtual environment
python -m venv venv

# Activate the virtual environment
.\venv\Scripts\Activate.ps1

# Upgrade pip
python -m pip install --upgrade pip

# Install dependencies
pip install -r requirements.txt
```

*(Note: If PowerShell execution policy restricts scripts, run `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` first).*

### 3. Launch the FastAPI Telemetry Server

```powershell
uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

Once running:
- **Interactive Swagger API Docs**: Open [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs) in your browser.
- **Session Telemetry Endpoint**: `GET http://127.0.0.1:8000/api/session-data?year=2024&gp=Monza&driver=VER`
- **Optimize Strategy Endpoint**: `POST http://127.0.0.1:8000/api/optimize-strategy`

---

## Mathematical Modeling & Racing Physics

### 1. Fuel Mass Burn-off Effect
Formula 1 cars start the Grand Prix with approximately 105–110 kg of fuel.
- **Fuel burn rate**: $\approx 1.95 \text{ kg/lap}$.
- **Pace sensitivity**: $\approx 0.033 \text{ s/kg}$ ($0.064 \text{ s/lap}$ pace gain).
- As fuel is consumed, the car becomes significantly lighter and faster, creating the classic bathtub lap-time curve that masks tire degradation early in a stint.

### 2. Tire Degradation Formulation
Degradation for compound $c$ at tire age $t$ is calculated nonlinearly:
$$\Delta t_{\text{wear}} = k_{\text{linear}} \cdot t + k_{\text{quad}} \cdot t^2 + \max(0, t - t_{\text{cliff}})^2 \cdot k_{\text{cliff}}$$

Where:
- **Soft (C4/C5)**: $k_{\text{linear}} = 0.095 \text{ s/lap}$, $t_{\text{cliff}} = 18 \text{ laps}$.
- **Medium (C3)**: $k_{\text{linear}} = 0.055 \text{ s/lap}$, $t_{\text{cliff}} = 28 \text{ laps}$.
- **Hard (C2)**: $k_{\text{linear}} = 0.032 \text{ s/lap}$, $t_{\text{cliff}} = 42 \text{ laps}$.

### 3. Safety Car "Cheap Pit Stop" Delta
- Under green flag racing, pit lane delta time loss is $\approx 22.0\text{s} - 24.5\text{s}$.
- When a Safety Car is deployed, on-track car speeds are reduced by $\approx 30\%$, cutting effective pit loss to $\approx 11.2\text{s} - 12.0\text{s}$.
- Pitting under Safety Car yields an instantaneous $+11.5\text{s}$ advantage over competitors who pitted under green flag conditions.

---

## Data Attribution & Acknowledgments
- **FastF1**: Python package for historical Formula 1 timing, telemetry, and weather data.
- **FIA & Formula One Management**: Historical session results and track characteristics.
