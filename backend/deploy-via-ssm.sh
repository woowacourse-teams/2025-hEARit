#!/bin/bash

# =================================================================
# SSM Send-Command를 통해 EC2 인스턴스에서 실행될 배포 스크립트
# =================================================================

# --- 1. 환경 변수 설정  ---
S3_BUCKET="hearit-artifacts"     # Artifact가 저장된 S3 버킷 이름
SERVICE_NAME="hearit-dev"        # Systemd 서비스 이름
APP_PATH="/home/ubuntu/hearit"   # JAR 파일이 최종적으로 위치할 디렉토리
REGION="ap-northeast-2"          # EC2 인스턴스의 리전 


echo "--- 1. 기존 서비스 중지 시작 ---"
# 서비스가 실행 중인지 확인하고 중지합니다.
sudo systemctl stop $SERVICE_NAME

if [ $? -ne 0 ]; then
  echo "경고: $SERVICE_NAME 서비스 중지 실패 또는 이미 중지됨. 계속 진행합니다."
fi


echo "--- 2. S3에서 최신 Artifact 다운로드 시작 ---"
# EC2 인스턴스에 연결된 IAM 역할을 사용하여 S3 파일 다운로드
aws s3 cp s3://$S3_BUCKET/app.jar $APP_PATH/app.jar --region $REGION

if [ $? -ne 0 ]; then
  echo "에러: S3 파일 다운로드 실패. 스크립트를 종료합니다."
  exit 1
fi
echo "Artifact 다운로드 완료: $APP_PATH/app.jar"


echo "--- 3. 서비스 재시작 시작 ---"
# Systemd 설정 파일 재로드 및 서비스 시작
sudo systemctl daemon-reload
sudo systemctl start $SERVICE_NAME


echo "--- 4. 서비스 상태 확인 ---"
# 서비스가 활성화되었는지 최종 확인
STATUS=$(sudo systemctl is-active $SERVICE_NAME)
if [ "$STATUS" = "active" ]; then
  echo "성공: $SERVICE_NAME 서비스가 성공적으로 시작되었습니다. (상태: $STATUS)"
else
  echo "실패: $SERVICE_NAME 서비스 시작 실패. (상태: $STATUS)"
  # 상세 로그 
  sudo journalctl -u $SERVICE_NAME --since "1 minute ago"
  exit 1
fi
