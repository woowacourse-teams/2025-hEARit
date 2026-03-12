package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.core.domain.RefreshToken;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("fake-test")
@Import({DbHelper.class, TestJpaAuditingConfig.class})
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private DbHelper dbHelper;


    @Test
    @DisplayName("활성 상태인 토큰이 여러 명일 때 각각의 UUID를 가져오는지 확인한다.")
    void findActiveMemberUuids_MultipleUsers() {
        // given
        LocalDateTime now = LocalDateTime.now();
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();

        refreshTokenRepository.save(new RefreshToken(member1, "token-1", now.plusDays(1)));
        refreshTokenRepository.save(new RefreshToken(member2, "token-2", now.plusDays(1)));

        // when
        List<UUID> activeMemberUuids = refreshTokenRepository.findActiveMemberUuids(now);

        // then
        assertThat(activeMemberUuids)
                .hasSize(2)
                .containsExactlyInAnyOrder(member1, member2);
    }

    @Test
    @DisplayName("만료일이 현재 시간보다 이후인 활성 멤버의 UUID 리스트를 조회한다.")
    void findActiveMemberUuids_Success() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 21, 0);

        UUID activeUuid1 = UUID.randomUUID();
        UUID activeUuid2 = UUID.randomUUID();
        refreshTokenRepository.save(new RefreshToken(activeUuid1, "token1", now.plusDays(7)));
        refreshTokenRepository.save(new RefreshToken(activeUuid2, "token2", now.plusSeconds(1)));

        UUID expiredUuid = UUID.randomUUID();
        refreshTokenRepository.save(new RefreshToken(expiredUuid, "token3", now.minusSeconds(1)));

        UUID edgeExpiredUuid = UUID.randomUUID();
        refreshTokenRepository.save(new RefreshToken(edgeExpiredUuid, "token4", now));

        // when
        List<UUID> activeMemberUuids = refreshTokenRepository.findActiveMemberUuids(now);

        // then
        assertThat(activeMemberUuids)
                .hasSize(2)
                .containsExactlyInAnyOrder(activeUuid1, activeUuid2)
                .doesNotContain(expiredUuid, edgeExpiredUuid);
    }
}
