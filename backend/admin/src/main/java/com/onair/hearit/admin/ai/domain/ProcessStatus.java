package com.onair.hearit.admin.ai.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProcessStatus {
    PENDING(0, "대기 중..."),
    UPLOADING(10, "파일 업로드 중..."),
    CONVERTING(25, "오디오 처리 중..."),
    TRANSCRIBING(50, "음성 인식 중... (1-2분 소요)"),
    CORRECTING(70, "대본 교정 중..."),
    GENERATING_META(85, "메타데이터 생성 중..."),
    COMPLETED(100, "처리 완료!"),
    FAILED(0, "처리 실패"),
    CONFIRMED(100, "등록 완료");

    private final int progress;
    private final String message;
}
