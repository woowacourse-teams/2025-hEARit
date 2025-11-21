#!/bin/bash

# =================================================================
# SSM Send-Command를 통해 EC2 인스턴스에서 실행될 배포 스크립트
# =================================================================

# --- 1. 환경 변수 설정 ---
SERVICE_NAME="hearit-dev"        # Systemd 서비스 이름
APP_PATH="/home/ubuntu/hearit"   # JAR 파일이 최종적으로 위치한 디렉토리


echo "--- 1. 기존 서비스 중지 시작 ---"
sudo systemctl stop $SERVICE_NAME

if [ $? -ne 0 ]; then
  echo "경고: $SERVICE_NAME 서비스 중지 실패 또는 이미 중지됨. 계속 진행합니다."
fi



echo "--- 2. 서비스 재시작 시작 ---"
sudo systemctl daemon-reload
sudo systemctl start $SERVICE_NAME


echo "--- 3. 서비스 상태 확인 ---"
STATUS=$(sudo systemctl is-active $SERVICE_NAME)
if [ "$STATUS" = "active" ]; then
  echo "성공: $SERVICE_NAME 서비스가 성공적으로 시작되었습니다. (상태: $STATUS)"
else
  echo "실패: $SERVICE_NAME 서비스 시작 실패. (상태: $STATUS)"
  # 상세 로그
  sudo journalctl -u $SERVICE_NAME --since "1 minute ago"
  exit 1
fi
