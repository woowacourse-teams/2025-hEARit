package com.onair.hearit.admin.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProcessStatusUtilTest {

    @ParameterizedTest
    @CsvSource({
            "PENDING, 0",
            "UPLOADING, 10",
            "CONVERTING, 25",
            "TRANSCRIBING, 50",
            "CORRECTING, 70",
            "GENERATING_META, 85",
            "COMPLETED, 100",
            "FAILED, 0",
            "CONFIRMED, 100"
    })
    @DisplayName("각 상태별 진행률을 올바르게 반환한다")
    void getProgress_returnsCorrectProgressForEachStatus(String statusName, int expectedProgress) {
        // given
        ProcessStatus status = ProcessStatus.valueOf(statusName);

        // when
        int progress = ProcessStatusUtil.getProgress(status);

        // then
        assertThat(progress).isEqualTo(expectedProgress);
    }

    @ParameterizedTest
    @CsvSource({
            "PENDING, 대기 중...",
            "UPLOADING, 파일 업로드 중...",
            "CONVERTING, 오디오 처리 중...",
            "TRANSCRIBING, 음성 인식 중... (1-2분 소요)",
            "CORRECTING, 대본 교정 중...",
            "GENERATING_META, 메타데이터 생성 중...",
            "COMPLETED, 처리 완료!",
            "FAILED, 처리 실패",
            "CONFIRMED, 등록 완료"
    })
    @DisplayName("각 상태별 메시지를 올바르게 반환한다")
    void getStatusMessage_returnsCorrectMessageForEachStatus(String statusName, String expectedMessage) {
        // given
        ProcessStatus status = ProcessStatus.valueOf(statusName);

        // when
        String message = ProcessStatusUtil.getStatusMessage(status);

        // then
        assertThat(message).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("진행률은 0에서 100 사이의 값이다")
    void getProgress_returnsValueBetween0And100() {
        for (ProcessStatus status : ProcessStatus.values()) {
            int progress = ProcessStatusUtil.getProgress(status);
            assertThat(progress).isBetween(0, 100);
        }
    }
}
