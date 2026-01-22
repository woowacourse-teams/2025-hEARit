package com.onair.hearit.app.member.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.member.dto.MemberInfoResponse;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import java.util.UUID;
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
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // when
        MemberInfoResponse response = memberService.getMember(member.getUuid());

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
        UUID nonExistId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> memberService.getMember(nonExistId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("memberUuid");
    }
}
