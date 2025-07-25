package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.MemberInfoResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class MemberServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setup() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    @DisplayName("회원 정보를 ID로 조회할 수 있다.")
    void getMemberById_localMember() {
        // given
        Member member = saveMember();

        // when
        MemberInfoResponse response = memberService.getMember(member.getId());

        // then
        assertAll(() -> {
            assertThat(response.id()).isEqualTo(member.getId());
            assertThat(response.nickname()).isEqualTo(member.getNickname());
            assertThat(response.profileImage()).isEqualTo(member.getProfileImage());
        });
    }

    @Test
    @DisplayName("존재하지 않는 ID로 회원 정보 조회 시 404 예외를 던진다.")
    void getMemberById() {
        // given
        Long nonExistId = 999L;

        // when & then
        assertThatThrownBy(() -> memberService.getMember(nonExistId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("memberId");
    }

    private Member saveMember() {
        return dbHelper.insertMember(TestFixture.createFixedMember());
    }
}
