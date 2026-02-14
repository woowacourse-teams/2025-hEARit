package com.onair.hearit.app.explore.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.core.infrastructure.jpa.RefreshTokenRepository;
import com.onair.hearit.core.log.logger.JsonLogger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExploreRankingBatchServiceTest {

    @InjectMocks
    private ExploreRankingBatchService batchService;

    @Mock
    private JsonLogger jsonLogger;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PlayingHistoryRepository playingHistoryRepository;

    @Mock
    private ExploreScoreInitializer exploreScoreInitializer;

    @Test
    @DisplayName("성공 시나리오: 타겟 유저들을 정상 추출하고 타입에 맞게 초기화를 수행한다.")
    void runRanking_success() {
        // given
        UUID memberUuid = UUID.randomUUID();
        UUID guestUuid = UUID.randomUUID();

        Member mockMember = mock(Member.class);
        doReturn(List.of(memberUuid)).when(refreshTokenRepository).findActiveMemberUuids(any());
        doReturn(List.of(guestUuid)).when(playingHistoryRepository).findUuidsByUpdatedAtAfter(any());

        doReturn(Optional.of(mockMember)).when(memberRepository).findByUuid(memberUuid);
        doReturn(Optional.empty()).when(memberRepository).findByUuid(guestUuid);

        // when
        batchService.runRankingForExplore();

        // then: Step 1 & 2 실행 검증
        verify(exploreScoreInitializer, times(1)).initializeScores(memberUuid, UserType.MEMBER);
        verify(exploreScoreInitializer, times(1)).initializeScores(guestUuid, UserType.GUEST);
        verify(jsonLogger, times(4)).info(any());
    }

    @Test
    @DisplayName("실패 시나리오 (Step 1): 타겟 UUID 추출 중 예외 발생 시 로그를 남기고 중단한다.")
    void runRanking_fail_at_step1() {
        // given
        doThrow(new RuntimeException("DB Fetch Error"))
                .when(refreshTokenRepository).findActiveMemberUuids(any());

        // when & then
        assertThatThrownBy(() -> batchService.runRankingForExplore())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB Fetch Error");

        // 초기화 로직이 실행되지 않았는지 확인
        verify(exploreScoreInitializer, never()).initializeScores(any(), any());

        // 에러 로그 호출 확인
        verify(jsonLogger, times(1)).error(any(), any());
    }

    @Test
    @DisplayName("실패 시나리오 (Step 2): 스코어 초기화 중 예외 발생 시 에러 로그를 남기고 예외를 던진다.")
    void runRanking_fail_at_step2() {
        // given
        UUID uuid = UUID.randomUUID();
        doReturn(List.of(uuid)).when(refreshTokenRepository).findActiveMemberUuids(any());
        doReturn(List.of()).when(playingHistoryRepository).findUuidsByUpdatedAtAfter(any());
        doReturn(Optional.empty()).when(memberRepository).findByUuid(uuid);

        doThrow(new RuntimeException("Calculation Error"))
                .when(exploreScoreInitializer).initializeScores(any(), any());

        // when & then
        assertThatThrownBy(() -> batchService.runRankingForExplore())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Calculation Error");

        // Step 1은 실행되었어야 함
        verify(refreshTokenRepository, times(1)).findActiveMemberUuids(any());

        // 에러 로그 확인
        verify(jsonLogger, times(1)).error(any(), any());
    }

    @Test
    @DisplayName("중복 제거 검증: 동일한 UUID가 멤버와 게스트 목록에 모두 있어도 초기화는 한 번만 수행된다.")
    void runRanking_deduplication() {
        // given
        UUID duplicateUuid = UUID.randomUUID();
        doReturn(List.of(duplicateUuid)).when(refreshTokenRepository).findActiveMemberUuids(any());
        doReturn(List.of(duplicateUuid)).when(playingHistoryRepository).findUuidsByUpdatedAtAfter(any());
        doReturn(Optional.empty()).when(memberRepository).findByUuid(duplicateUuid);

        // when
        batchService.runRankingForExplore();

        // then: Set에 의해 중복 제거되어 한 번만 호출됨
        verify(exploreScoreInitializer, times(1)).initializeScores(duplicateUuid, UserType.GUEST);
    }
}
