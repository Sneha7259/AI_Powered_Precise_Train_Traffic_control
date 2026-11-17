from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(title="SIH25022 Delay & Conflict Scoring API", version="0.1.0")

class Candidate(BaseModel):
    priority: int = Field(ge=1, le=10)
    delay_minutes: int = Field(ge=0)
    speed_kmph: float = Field(gt=0)
    distance_to_loop_km: float = Field(ge=0)
    opposing_train_distance_km: float = Field(ge=0)
    train_type: str = "PASSENGER"

@app.get("/health")
def health():
    return {"status": "UP", "model": "baseline-heuristic", "mode": "simulation-only"}

@app.post("/score")
def score(candidate: Candidate):
    # Transparent baseline score. Replace with a trained and validated XGBoost model.
    priority_score = max(0, 11 - candidate.priority) * 4.0
    delay_score = min(candidate.delay_minutes, 120) * 0.25
    loop_score = max(0, 8 - candidate.distance_to_loop_km) * 1.5
    conflict_risk = max(0, 5 - candidate.opposing_train_distance_km) * 5.0
    freight_penalty = 4.0 if candidate.train_type.upper() == "FREIGHT" else 0.0
    score = priority_score + delay_score + loop_score - conflict_risk - freight_penalty
    return {
        "precedence_score": round(score, 2),
        "conflict_risk": "HIGH" if candidate.opposing_train_distance_km < 2 else
                         "MEDIUM" if candidate.opposing_train_distance_km < 5 else "LOW",
        "explanation": "Baseline transparent scoring only; not a certified safety model.",
        "model_version": "baseline-heuristic-0.1"
    }
