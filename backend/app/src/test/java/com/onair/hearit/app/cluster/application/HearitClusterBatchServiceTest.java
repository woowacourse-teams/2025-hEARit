package com.onair.hearit.app.cluster.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.onair.hearit.core.infrastructure.jdbc.HearitClusterCommandRepository;
import com.onair.hearit.core.log.logger.JsonLogger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HearitClusterBatchServiceTest {

    @InjectMocks
    private HearitClusterBatchService batchService;

    @Mock
    private JsonLogger jsonLogger;

    @Mock
    private HearitClusterCommandRepository hearitClusterCommandRepository;

    @Mock
    private HearitClusterFeatureLoader hearitClusterFeatureLoader;

    @Mock
    private HearitClusterCalculator hearitClusterCalculator;

    @Test
    @DisplayName("성공 시나리오: 모든 스텝이 순서대로 실행되고 성공 로그가 기록된다.")
    void runClustering_success() {
        // given & when
        batchService.runClustering();

        // then: 호출 순서와 인자 검증
        verify(hearitClusterFeatureLoader, times(1)).loadStatisticsFeature(100); // CHUNK_SIZE
        verify(hearitClusterCalculator, times(1)).calculateClusters(5); // K_SIZE

        // 시작, 진행(2회), 완료 로그가 찍혔는지 확인
        verify(jsonLogger, times(5)).info(any());
    }

    @Test
    @DisplayName("실패 시나리오 (Step 1): 데이터 로드 중 예외 발생 시 로그를 남기고 중단된다.")
    void runClustering_fail_at_step1() {
        // given
        doThrow(new RuntimeException("DB Connection Fail"))
                .when(hearitClusterFeatureLoader).loadStatisticsFeature(anyInt());

        // when & then
        assertThatThrownBy(() -> batchService.runClustering())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB Connection Fail");

        // Step 2는 실행되지 않아야 함
        verify(hearitClusterCalculator, never()).calculateClusters(anyInt());

        // 에러 로그 호출 확인
        verify(jsonLogger, times(1)).error(any(), any());
    }

    @Test
    @DisplayName("실패 시나리오 (Step 2): 계산 중 예외 발생 시 로그를 남긴다.")
    void runClustering_fail_at_step2() {
        // given
        doNothing().when(hearitClusterFeatureLoader).loadStatisticsFeature(anyInt());
        doThrow(new RuntimeException("K-Means Logic Error"))
                .when(hearitClusterCalculator).calculateClusters(anyInt());

        // when & then
        assertThatThrownBy(() -> batchService.runClustering())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("K-Means Logic Error");

        // Step 1은 실행되었고, 에러 로그가 남아야 함
        verify(hearitClusterFeatureLoader, times(1)).loadStatisticsFeature(anyInt());
        verify(jsonLogger, times(1)).error(any(), any());
    }
}
