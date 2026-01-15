package com.onair.hearit.admin.ai.domain;

import java.util.Map;

public final class ProcessStatusUtil {

    private static final Map<ProcessStatus, Integer> PROGRESS_MAP = Map.of(
            ProcessStatus.PENDING, 0,
            ProcessStatus.UPLOADING, 10,
            ProcessStatus.CONVERTING, 25,
            ProcessStatus.TRANSCRIBING, 50,
            ProcessStatus.CORRECTING, 70,
            ProcessStatus.GENERATING_META, 85,
            ProcessStatus.COMPLETED, 100,
            ProcessStatus.FAILED, 0,
            ProcessStatus.CONFIRMED, 100
    );

    private ProcessStatusUtil() {
        // Utility class
    }

    public static int getProgress(ProcessStatus status) {
        return PROGRESS_MAP.getOrDefault(status, 0);
    }

    public static String getStatusMessage(ProcessStatus status) {
        return switch (status) {
            case PENDING -> "대기 중...";
            case UPLOADING -> "파일 업로드 중...";
            case CONVERTING -> "오디오 처리 중...";
            case TRANSCRIBING -> "음성 인식 중... (1-2분 소요)";
            case CORRECTING -> "대본 교정 중...";
            case GENERATING_META -> "메타데이터 생성 중...";
            case COMPLETED -> "처리 완료!";
            case FAILED -> "처리 실패";
            case CONFIRMED -> "등록 완료";
        };
    }
}
