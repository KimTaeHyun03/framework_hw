"""FastAPI 진입점. Spring Boot가 WebClient로 호출하는 ML 서버.

엔드포인트
  GET  /health   — 헬스체크 (학습 여부 포함)
  POST /train    — 고정 MLP 학습, 정확도/손실률 반환
  POST /predict  — 학습된 모델로 예측
"""

from fastapi import FastAPI, HTTPException

from .model import experiment_model
from .schemas import (
    PredictRequest,
    PredictResponse,
    TrainRequest,
    TrainResponse,
)

app = FastAPI(title="ML Experiment Server", version="0.1.0")


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "trained": experiment_model.is_trained}


@app.post("/train", response_model=TrainResponse)
def train(req: TrainRequest) -> TrainResponse:
    result = experiment_model.train(
        epochs=req.epochs,
        batch_size=req.batch_size,
        learning_rate=req.learning_rate,
    )
    return TrainResponse(**result)


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest) -> PredictResponse:
    try:
        result = experiment_model.predict(req.features)
    except RuntimeError as e:
        # 학습 전 예측 요청 → 409 Conflict
        raise HTTPException(status_code=409, detail=str(e))
    return PredictResponse(**result)
