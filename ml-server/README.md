# ML Server (Python / FastAPI)

머신러닝 실험 이력 시스템의 **학습/예측 담당** 서버. Spring Boot가 `WebClient`로
HTTP 호출하며, 이 서버는 DB나 soft-delete 같은 도메인 로직을 갖지 않는다.
(실험 로그 저장·CRUD는 전부 Spring 영역)

## 설계 고정값

- 알고리즘: **MLP** (작은 신경망), 옵티마이저: **Adam** — 하나로 고정
- 프레임워크: **TensorFlow / Keras**
- 데이터셋: Iris (4 feature, 3 class) — 초 단위 학습
- 노출 하이퍼파라미터: `epochs`, `batch_size`, `learning_rate` 3개로 한정

## 실행

```bash
./run.sh
```

처음 실행하면 `.venv` 생성 → 의존성 설치 → 서버 기동을 한 번에 처리한다.
포트를 바꾸려면 `PORT=9000 ./run.sh`.

<details><summary>수동 실행 (스크립트 없이)</summary>

```bash
cd ml-server
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```
</details>

API 문서: http://localhost:8000/docs

## 엔드포인트

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/health` | 헬스체크 (`trained` 여부 포함) |
| POST | `/train` | 고정 MLP 학습 → 정확도/손실률 반환 |
| POST | `/predict` | 학습된 모델로 예측 (학습 전이면 409) |

### `/train`

요청 (모두 선택, 미지정 시 기본값):
```json
{ "epochs": 50, "batch_size": 16, "learning_rate": 0.01 }
```
응답 — Spring이 실험 로그로 저장하는 값:
```json
{
  "algorithm": "MLP",
  "hyperparameters": { "optimizer": "adam", "epochs": 50, "batch_size": 16, "learning_rate": 0.01 },
  "accuracy": 0.9667,
  "loss": 0.1234
}
```

### `/predict`

```json
{ "features": [5.1, 3.5, 1.4, 0.2] }
```
```json
{ "predicted_class": "setosa", "predicted_index": 0, "probabilities": [0.98, 0.01, 0.01] }
```

## curl 예시

```bash
curl -X POST localhost:8000/train  -H 'Content-Type: application/json' -d '{"epochs":50}'
curl -X POST localhost:8000/predict -H 'Content-Type: application/json' -d '{"features":[5.1,3.5,1.4,0.2]}'
```
