"""
Pit-Stop Strategy Simulator and Optimizer.
Evaluates 1-stop, 2-stop, and 3-stop permutations under green flag and safety-car scenarios.
"""

from typing import List, Dict, Any


class StrategyOptimizer:
    COMPOUND_PARAMS = {
        "SOFT": {"delta": 0.0, "wear": 0.095, "cliff": 18},
        "MEDIUM": {"delta": 0.42, "wear": 0.055, "cliff": 28},
        "HARD": {"delta": 0.92, "wear": 0.032, "cliff": 42},
    }

    def simulate_strategies(
        self,
        total_laps: int = 53,
        base_lap: float = 81.250,
        pit_lane_loss: float = 23.5,
        sc_pit_loss: float = 12.0,
        safety_car_lap: int = None
    ) -> List[Dict[str, Any]]:
        candidates = [
            {"name": "Plan A (1-Stop): Medium -> Hard", "compounds": ["MEDIUM", "HARD"], "pit_laps": [int(total_laps * 0.38)]},
            {"name": "Plan B (1-Stop): Soft -> Hard", "compounds": ["SOFT", "HARD"], "pit_laps": [int(total_laps * 0.28)]},
            {"name": "Plan C (1-Stop): Hard -> Medium", "compounds": ["HARD", "MEDIUM"], "pit_laps": [int(total_laps * 0.62)]},
            {"name": "Plan D (2-Stop): Soft -> Hard -> Medium", "compounds": ["SOFT", "HARD", "MEDIUM"], "pit_laps": [int(total_laps * 0.25), int(total_laps * 0.58)]},
            {"name": "Plan E (2-Stop): Medium -> Hard -> Medium", "compounds": ["MEDIUM", "HARD", "MEDIUM"], "pit_laps": [int(total_laps * 0.30), int(total_laps * 0.65)]},
        ]

        results = []
        for cand in candidates:
            res = self._evaluate_candidate(cand, total_laps, base_lap, pit_lane_loss, sc_pit_loss, safety_car_lap)
            results.append(res)

        results.sort(key=lambda x: x["total_race_time_seconds"])
        best_time = results[0]["total_race_time_seconds"]

        for idx, item in enumerate(results):
            delta = item["total_race_time_seconds"] - best_time
            item["delta_to_best_seconds"] = round(delta, 3)
            item["formatted_delta"] = "0.000s" if idx == 0 else f"+{delta:.3f}s"
            item["is_recommended"] = (idx == 0)

        return results

    def _evaluate_candidate(self, candidate, total_laps, base_lap, pit_loss, sc_pit_loss, sc_lap):
        stints = []
        pit_laps = candidate["pit_laps"]
        compounds = candidate["compounds"]

        cur_start = 1
        for i, comp in enumerate(compounds):
            end_lap = pit_laps[i] if i < len(pit_laps) else total_laps
            stints.append({"compound": comp, "start_lap": cur_start, "end_lap": end_lap, "laps": end_lap - cur_start + 1})
            cur_start = end_lap + 1

        total_time = 0.0
        for stint in stints:
            comp = stint["compound"]
            params = self.COMPOUND_PARAMS[comp]
            for age in range(1, stint["laps"] + 1):
                deg = params["wear"] * age
                if age > params["cliff"]:
                    deg += 0.12 * ((age - params["cliff"]) ** 2)
                fuel_mass = (total_laps - age) * 1.95
                fuel_gain = (total_laps * 1.95 - fuel_mass) * 0.033
                lap_time = base_lap + params["delta"] + deg - fuel_gain
                total_time += lap_time

        # Pit stop losses
        total_pit_loss = 0.0
        for p in pit_laps:
            loss = sc_pit_loss if (sc_lap and abs(p - sc_lap) <= 2) else pit_loss
            total_pit_loss += loss
            total_time += loss

        hours = int(total_time // 3600)
        minutes = int((total_time % 3600) // 60)
        seconds = total_time % 60

        return {
            "plan_name": candidate["name"],
            "stop_count": len(compounds) - 1,
            "stints": stints,
            "pit_laps": pit_laps,
            "total_race_time_seconds": round(total_time, 3),
            "formatted_total_time": f"{hours}:{minutes:02d}:{seconds:06.3f}",
            "average_lap_time_seconds": round(total_time / total_laps, 3),
            "total_pit_loss_seconds": total_pit_loss,
            "undercut_window": f"Laps {pit_laps[0]-2} to {pit_laps[0]+1}" if pit_laps else "N/A"
        }
