"""Spring Boot ↔ ML 서버 간 JSON 계약 (요청/응답 스키마).

ML 서버는 실험 로그 중 알고리즘 / 하이퍼파라미터 / 정확도 / 손실률 컬럼만 책임진다.
날짜·메모/태그·삭제여부는 Spring 도메인이므로 여기서 다루지 않는다.
"""

from pydantic import BaseModel, Field

from . import config


class TrainRequest(BaseModel):
    """학습 요청. 모든 값은 선택이며 미지정 시 기본값 사용."""

    epochs: int = Field(default=config.DEFAULT_EPOCHS, ge=1, le=500)
    batch_size: int = Field(default=config.DEFAULT_BATCH_SIZE, ge=1, le=256)
    learning_rate: float = Field(default=config.DEFAULT_LEARNING_RATE, gt=0, le=1)


class TrainResponse(BaseModel):
    """학습 결과. Spring이 이 값을 실험 로그로 H2에 저장한다."""

    algorithm: str
    hyperparameters: dict
    accuracy: float  # 0.0 ~ 1.0
    loss: float


class PredictRequest(BaseModel):
    """예측 요청. Iris feature 4개 (sepal/petal length·width)."""

    features: list[float] = Field(
        ..., min_length=config.NUM_FEATURES, max_length=config.NUM_FEATURES
    )


class PredictResponse(BaseModel):
    predicted_class: str
    predicted_index: int
    probabilities: list[float]
