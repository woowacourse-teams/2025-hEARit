package com.onair.hearit.admin.ai.domain;

public enum ProcessStatus {
    PENDING,           // 대기 중
    UPLOADING,         // 원본 파일 S3 업로드 중
    CONVERTING,        // MP3 변환 + 쇼츠 생성 중
    TRANSCRIBING,      // STT 처리 중
    CORRECTING,        // 대본 교정 중
    GENERATING_META,   // 메타데이터 생성 중
    COMPLETED,         // 처리 완료 (검토 대기)
    FAILED,            // 처리 실패
    CONFIRMED          // 검토 완료 및 Hearit 등록됨
}
