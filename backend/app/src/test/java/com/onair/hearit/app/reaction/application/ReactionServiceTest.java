package com.onair.hearit.app.reaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.exception.custom.UnauthorizedException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.Reaction;
import com.onair.hearit.core.domain.ReactionType;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import com.onair.hearit.core.infrastructure.jpa.ReactionRepository;
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
class ReactionServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    private ReactionService reactionService;

    @BeforeEach
    void setup() {
        reactionService = new ReactionService(hearitRepository, reactionRepository);
    }

    @Test
    @DisplayName("멤버가 히어릿에 좋아요를 추가한다.")
    void addReactionTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        UserInfo memberInfo = RequestUser.member(member.getUuid()).getUserInfo();
        int previousReactionCount = reactionRepository.findAll().size();

        // when
        reactionService.addReaction(memberInfo, hearit.getId(), ReactionType.LIKE);

        // then
        int currentReactionCount = reactionRepository.findAll().size();
        assertAll(
                () -> assertThat(previousReactionCount + 1).isEqualTo(currentReactionCount),
                () -> assertThat(reactionRepository.existsByHearitAndUserUuidAndType(
                        hearit, member.getUuid(), ReactionType.LIKE)).isTrue()
        );
    }

    @Test
    @DisplayName("좋아요 추가 시, 비회원인 경우 UnauthorizedException을 던진다.")
    void addReaction_GuestTest() {
        // given
        RequestUser guest = RequestUser.guest("00000000-0000-0000-0000-000000000000");
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Long hearitId = hearit.getId();

        // when & then
        assertThatThrownBy(() -> reactionService.addReaction(guest.getUserInfo(), hearitId, ReactionType.LIKE))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("비회원은 좋아요를 추가할 권한이 없습니다.");
    }

    @Test
    @DisplayName("좋아요 추가 시, 존재하지 않는 히어릿이면 NotFoundException을 던진다.")
    void addReaction_NotFoundHearitTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = RequestUser.member(member.getUuid()).getUserInfo();
        Long invalidHearitId = 999L;

        // when & then
        assertThatThrownBy(() -> reactionService.addReaction(memberInfo, invalidHearitId, ReactionType.LIKE))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("hearitId");
    }

    @Test
    @DisplayName("좋아요 추가 시, 이미 좋아요가 존재하면 중복 추가하지 않는다.")
    void addReaction_AlreadyExistTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        UserInfo memberInfo = RequestUser.member(member.getUuid()).getUserInfo();

        Reaction reaction = new Reaction(member.getUuid(), hearit, ReactionType.LIKE);
        dbHelper.insertReaction(reaction);

        int previousReactionCount = reactionRepository.findAll().size();

        // when
        reactionService.addReaction(memberInfo, hearit.getId(), ReactionType.LIKE);

        // then
        int currentReactionCount = reactionRepository.findAll().size();
        assertThat(previousReactionCount).isEqualTo(currentReactionCount);
    }

    @Test
    @DisplayName("멤버가 히어릿에 추가한 좋아요를 삭제한다.")
    void removeReactionTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        Reaction reaction = new Reaction(member.getUuid(), hearit, ReactionType.LIKE);
        dbHelper.insertReaction(reaction);

        UserInfo memberInfo = RequestUser.member(member.getUuid()).getUserInfo();

        // when
        reactionService.removeReaction(memberInfo, hearit.getId(), ReactionType.LIKE);

        // then
        assertThat(reactionRepository.existsByHearitAndUserUuidAndType(
                hearit, member.getUuid(), ReactionType.LIKE)).isFalse();
    }

    @Test
    @DisplayName("좋아요 삭제 시, 비회원인 경우 UnauthorizedException을 던진다.")
    void removeReaction_GuestTest() {
        // given
        RequestUser guest = RequestUser.guest("00000000-0000-0000-0000-000000000000");
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Long hearitId = hearit.getId();

        // when & then
        assertThatThrownBy(() -> reactionService.removeReaction(guest.getUserInfo(), hearitId, ReactionType.LIKE))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("비회원은 좋아요를 삭제할 권한이 없습니다.");
    }

    @Test
    @DisplayName("좋아요 삭제 시, 존재하지 않는 히어릿이면 NotFoundException을 던진다.")
    void removeReaction_NotFoundHearitTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = RequestUser.member(member.getUuid()).getUserInfo();
        Long invalidHearitId = 999L;

        // when & then
        assertThatThrownBy(() -> reactionService.removeReaction(memberInfo, invalidHearitId, ReactionType.LIKE))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("hearitId");
    }

    @Test
    @DisplayName("좋아요 삭제 시, 좋아요가 존재하지 않아도 예외가 발생하지 않는다.")
    void removeReaction_NotExistReactionTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        UserInfo memberInfo = RequestUser.member(member.getUuid()).getUserInfo();

        // when & then
        reactionService.removeReaction(memberInfo, hearit.getId(), ReactionType.LIKE);
    }
}
