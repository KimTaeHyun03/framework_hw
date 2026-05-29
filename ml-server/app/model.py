"""고정 MLP 모델의 학습/예측 로직.

알고리즘(MLP)과 옵티마이저(Adam)는 고정. 노출하는 하이퍼파라미터는
epochs / batch_size / learning_rate 세 개로 한정해 테이블 컬럼 폭증을 막는다.
모델은 프로세스 메모리에 상주시킨다 (별도 DB·영속화 없음).
"""

import numpy as np
import tensorflow as tf
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

from . import config


class ExperimentModel:
    """학습된 모델 + 전처리 스케일러를 함께 들고 있는 메모리 상주 컨테이너."""

    def __init__(self) -> None:
        self._model: tf.keras.Model | None = None
        self._scaler: StandardScaler | None = None

    @property
    def is_trained(self) -> bool:
        return self._model is not None

    def train(self, epochs: int, batch_size: int, learning_rate: float) -> dict:
        """Iris 데이터로 학습하고 테스트셋 정확도/손실률을 반환한다."""
        X, y = load_iris(return_X_y=True)
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y
        )

        scaler = StandardScaler().fit(X_train)
        X_train = scaler.transform(X_train)
        X_test = scaler.transform(X_test)

        model = tf.keras.Sequential(
            [
                tf.keras.layers.Input(shape=(config.NUM_FEATURES,)),
                tf.keras.layers.Dense(16, activation="relu"),
                tf.keras.layers.Dense(8, activation="relu"),
                tf.keras.layers.Dense(config.NUM_CLASSES, activation="softmax"),
            ]
        )
        model.compile(
            optimizer=tf.keras.optimizers.Adam(learning_rate=learning_rate),
            loss="sparse_categorical_crossentropy",
            metrics=["accuracy"],
        )
        model.fit(
            X_train,
            y_train,
            epochs=epochs,
            batch_size=batch_size,
            verbose=0,
        )

        loss, accuracy = model.evaluate(X_test, y_test, verbose=0)

        self._model = model
        self._scaler = scaler

        return {
            "algorithm": config.ALGORITHM,
            "hyperparameters": {
                "optimizer": config.OPTIMIZER,
                "epochs": epochs,
                "batch_size": batch_size,
                "learning_rate": learning_rate,
            },
            "accuracy": round(float(accuracy), 4),
            "loss": round(float(loss), 4),
        }

    def predict(self, features: list[float]) -> dict:
        """학습된 모델로 단일 샘플을 예측한다. 학습 전이면 RuntimeError."""
        if not self.is_trained:
            raise RuntimeError("모델이 아직 학습되지 않았습니다. 먼저 /train을 호출하세요.")

        x = self._scaler.transform(np.array([features], dtype=float))
        probs = self._model.predict(x, verbose=0)[0]
        index = int(np.argmax(probs))

        return {
            "predicted_class": config.CLASS_NAMES[index],
            "predicted_index": index,
            "probabilities": [round(float(p), 4) for p in probs],
        }


# 프로세스 전역 단일 인스턴스 (메모리 상주 모델)
experiment_model = ExperimentModel()
