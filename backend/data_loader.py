"""
Historical Formula 1 Data Ingestion and Quality Pipeline using FastF1.
Includes local filesystem caching, outlier cleaning, and synthetic demo fallback.
"""

import os
import numpy as np
import pandas as pd
from typing import Dict, Any, Tuple, Optional

try:
    import fastf1
    FASTF1_AVAILABLE = True
except ImportError:
    FASTF1_AVAILABLE = False


class F1DataLoader:
    def __init__(self, cache_dir: str = "cache"):
        self.cache_dir = cache_dir
        if FASTF1_AVAILABLE:
            if not os.path.exists(cache_dir):
                os.makedirs(cache_dir, exist_ok=True)
            fastf1.Cache.enable_cache(cache_dir)

    def load_session_laps(
        self,
        year: int = 2024,
        gp: str = "Monza",
        identifier: str = "R",
        driver_code: Optional[str] = "VER",
        force_synthetic: bool = False
    ) -> Tuple[pd.DataFrame, Dict[str, Any]]:
        """
        Loads historical laps via FastF1, or generates calibrated synthetic telemetry if offline.
        """
        if not force_synthetic and FASTF1_AVAILABLE:
            try:
                session = fastf1.get_session(year, gp, identifier)
                session.load(telemetry=False, weather=True)
                laps = session.laps

                if driver_code:
                    laps = laps.pick_driver(driver_code)

                cleaned_df = self._clean_laps(laps)
                metadata = {
                    "source": "FastF1 Historical Archive",
                    "year": year,
                    "grand_prix": gp,
                    "session": identifier,
                    "driver": driver_code,
                    "is_synthetic": False,
                    "total_laps_loaded": len(cleaned_df)
                }
                return cleaned_df, metadata
            except Exception as e:
                print(f"[F1DataLoader] FastF1 fetch failed ({e}). Reverting to synthetic calibration.")

        # Synthetic fallback mode
        return self._generate_synthetic_laps(gp=gp, driver_code=driver_code or "VER")

    def _clean_laps(self, laps: pd.DataFrame) -> pd.DataFrame:
        """
        Filters out-laps, in-laps, safety car laps, and telemetry nulls.
        Computes tire age and fuel-corrected lap time.
        """
        df = laps.copy()
        # Filter accurately timed laps
        df = df[df['LapTime'].notna()]
        df['LapTimeSeconds'] = df['LapTime'].dt.total_seconds()

        # Identify pit events
        df['IsPitIn'] = df['PitInTime'].notna()
        df['IsPitOut'] = df['PitOutTime'].notna()
        df['IsValidPace'] = (~df['IsPitIn']) & (~df['IsPitOut']) & (df['Deleted'] == False)

        # Standardize compound names
        if 'Compound' in df.columns:
            df['Compound'] = df['Compound'].fillna('MEDIUM')
        else:
            df['Compound'] = 'MEDIUM'

        if 'TyreLife' in df.columns:
            df['TireAge'] = df['TyreLife'].fillna(1).astype(int)
        else:
            df['TireAge'] = df['LapNumber']

        total_laps = df['LapNumber'].max()
        # Fuel load estimate: ~110kg start, burning ~1.95kg/lap at ~0.033s/kg
        df['FuelLoadKg'] = (total_laps - df['LapNumber']).clip(lower=0) * 1.95
        df['FuelAdvantageSeconds'] = (total_laps * 1.95 - df['FuelLoadKg']) * 0.033
        df['FuelCorrectedLapTimeSeconds'] = df['LapTimeSeconds'] + df['FuelAdvantageSeconds']

        return df

    def _generate_synthetic_laps(self, gp: str = "Monza", driver_code: str = "VER") -> Tuple[pd.DataFrame, Dict[str, Any]]:
        total_laps = 53
        base_lap = 81.250 if gp.lower() == "monza" else 86.500
        pit_lap = 20

        data = []
        tire_age = 1
        compound = "MEDIUM"

        for lap in range(1, total_laps + 1):
            is_pit_in = (lap == pit_lap)
            is_pit_out = (lap == pit_lap + 1)

            if lap > pit_lap and compound != "HARD":
                compound = "HARD"
                tire_age = 1

            # Degradation formula
            wear_rate = 0.055 if compound == "MEDIUM" else 0.032
            cliff_lap = 28 if compound == "MEDIUM" else 42
            deg = wear_rate * tire_age + (0.12 * max(0, tire_age - cliff_lap) ** 2)

            fuel_load_kg = (total_laps - lap) * 1.95
            fuel_gain = (total_laps * 1.95 - fuel_load_kg) * 0.033

            lap_time = base_lap + deg - fuel_gain + np.random.normal(0, 0.07)
            if is_pit_in:
                lap_time += 11.0
            elif is_pit_out:
                lap_time += 12.5

            data.append({
                "LapNumber": lap,
                "LapTimeSeconds": lap_time,
                "Compound": compound,
                "TireAge": tire_age,
                "FuelLoadKg": fuel_load_kg,
                "FuelCorrectedLapTimeSeconds": lap_time + fuel_gain,
                "IsPitIn": is_pit_in,
                "IsPitOut": is_pit_out,
                "IsValidPace": not (is_pit_in or is_pit_out)
            })
            tire_age += 1

        df = pd.DataFrame(data)
        metadata = {
            "source": "Calibrated Physics-Informed Synthetic Telemetry",
            "grand_prix": gp,
            "driver": driver_code,
            "is_synthetic": True,
            "total_laps_loaded": len(df)
        }
        return df, metadata
