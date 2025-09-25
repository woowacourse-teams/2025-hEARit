package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.OAuthProvider;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class MemberRepositoryTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("localId로 활성 회원을 조회할 수 있다")
    void findByLocalId_whenActiveMember_thenReturnMember() {
        // given
        dbHelper.insertMember(Member.createLocalUser(UUID.randomUUID().toString(), "user123", "닉네임", "비번", null));

        // when
        Optional<Member> result = memberRepository.findByLocalId("user123");

        // then
        assertAll(() -> {
            assertThat(result).isPresent();
            assertThat(result.get().getLocalId()).isEqualTo("user123");
        });
    }

    @Test
    @DisplayName("localId로 탈퇴한 회원은 조회되지 않는다")
    void findByLocalId_whenDeletedMember_thenEmpty() {
        // given
        Member member = dbHelper.insertMember(
                Member.createLocalUser(UUID.randomUUID().toString(), "user123", "닉네임", "비번", null));
        memberRepository.save(member);
        member.withdraw();

        // when
        Optional<Member> result = memberRepository.findByLocalId("user123");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("socialId로 활성 회원을 조회할 수 있다")
    void findBySocialId_whenActiveMember_thenReturnMember() {
        // given
        OAuthProvider kakao = OAuthProvider.KAKAO;
        dbHelper.insertMember(Member.createSocialUser(UUID.randomUUID().toString(), "social123", "닉네임", null, kakao));

        // when
        Optional<Member> result = memberRepository.findBySocialIdAndOAuthProvider("social123", kakao);

        // then
        assertAll(() -> {
            assertThat(result).isPresent();
            assertThat(result.get().getSocialId()).isEqualTo("social123");
        });
    }

    @Test
    @DisplayName("localId가 존재하는 활성 회원이 있을 때 true를 반환한다")
    void existsByLocalId_whenActiveMember_thenTrue() {
        // given
        dbHelper.insertMember(Member.createLocalUser(UUID.randomUUID().toString(), "user123", "닉네임", "비번", null));

        // when
        boolean exists = memberRepository.existsByLocalId("user123");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("localId가 존재하더라도 탈퇴한 회원이면 false를 반환한다")
    void existsByLocalId_whenDeletedMember_thenFalse() {
        // given
        Member member = dbHelper.insertMember(
                Member.createLocalUser(UUID.randomUUID().toString(), "user123", "닉네임", "비번", null));
        member.withdraw();

        // when
        boolean exists = memberRepository.existsByLocalId("user123");

        // then
        assertThat(exists).isFalse();
    }
}
