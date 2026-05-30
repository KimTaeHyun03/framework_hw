#!/usr/bin/env bash
# ML 서버 실행 스크립트
# venv가 없으면 생성하고 의존성을 설치한 뒤 uvicorn으로 서버를 띄운다.
# 사용법: ./run.sh   (포트 변경: PORT=9000 ./run.sh)
set -euo pipefail

cd "$(dirname "$0")"

PORT="${PORT:-8000}"

# python3 확인
if ! command -v python3 >/dev/null 2>&1; then
  echo "[run] ERROR: python3 가 없습니다. (Ubuntu/WSL: sudo apt install -y python3)" >&2
  exit 1
fi

# .venv 가 없거나, 깨져서(uvicorn 없음) 정상이 아니면 (재)생성한다.
# 디렉토리 존재만 보면, 한 번 생성에 실패해 남은 빈 폴더 때문에 영원히 복구가 안 된다.
if [ ! -x .venv/bin/uvicorn ]; then
  echo "[run] 정상 venv 없음 → (재)생성 및 의존성 설치"
  rm -rf .venv
  python3 -m venv .venv
  # python3-venv 미설치 등으로 ensurepip 가 빠진 깨진 venv 방어
  if [ ! -x .venv/bin/pip ]; then
    echo "[run] ERROR: venv 에 pip 이 없습니다 (불완전한 venv)." >&2
    echo "       Ubuntu/WSL: sudo apt install -y python3-venv python3-pip" >&2
    rm -rf .venv
    exit 1
  fi
  ./.venv/bin/pip install --upgrade pip
  ./.venv/bin/pip install -r requirements.txt
fi

echo "[run] uvicorn 시작 (port ${PORT})"
exec ./.venv/bin/uvicorn app.main:app --reload --port "${PORT}"
