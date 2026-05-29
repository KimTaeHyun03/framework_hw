"""고정 설정값.

설계 제약: 알고리즘과 옵티마이저를 하나로 고정한다.
여러 알고리즘을 허용하면 하이퍼파라미터 컬럼에 NULL이 과도하게 늘어나기 때문.
"""

# 고정 알고리즘 / 옵티마이저 (실험 로그의 '알고리즘' 컬럼에 그대로 기록됨)
ALGORITHM = "MLP"
OPTIMIZER = "adam"

# 데이터셋 (Iris: 4 feature, 3 class, 초 단위 학습)
NUM_FEATURES = 4
NUM_CLASSES = 3
CLASS_NAMES = ["setosa", "versicolor", "virginica"]

# 하이퍼파라미터 기본값 — /train 요청에서 덮어쓸 수 있다.
DEFAULT_EPOCHS = 50
DEFAULT_BATCH_SIZE = 16
DEFAULT_LEARNING_RATE = 0.01
