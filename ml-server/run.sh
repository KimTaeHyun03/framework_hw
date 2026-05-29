#!/usr/bin/env bash
# ML 서버 실행 스크립트
# venv가 없으면 생성하고 의존성을 설치한 뒤 uvicorn으로 서버를 띄운다.
# 사용법: ./run.sh   (포트 변경: PORT=9000 ./run.sh)
set -euo pipefail

cd "$(dirname "$0")"

PORT="${PORT:-8000}"

if [ ! -d .venv ]; then
  echo "[run] .venv 없음 → 생성 및 의존성 설치"
  python3 -m venv .venv
  ./.venv/bin/pip install --upgrade pip
  ./.venv/bin/pip install -r requirements.txt
fi

echo "[run] uvicorn 시작 (port ${PORT})"
exec ./.venv/bin/uvicorn app.main:app --reload --port "${PORT}"
