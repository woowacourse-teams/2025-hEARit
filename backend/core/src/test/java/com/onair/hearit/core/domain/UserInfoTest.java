package com.onair.hearit.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.core.domain.exception.UserInfoDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserInfo 도메인 테스트")
class UserInfoTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTest {

        @Test
        @DisplayName("memberId만 있으면 MEMBER 타입으로 생성된다")
        void createWithMemberId() {
            UserInfo userInfo = new UserInfo(1L, null);

            assertThat(userInfo.isMember()).isTrue();
            assertThat(userInfo.isGuest()).isFalse();
            assertThat(userInfo.getMemberId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("guestId만 있으면 GUEST 타입으로 생성된다")
        void createWithGuestId() {
            String guestId = "123e4567-e89b-12d3-a456-426614174000"; // 36자
            UserInfo userInfo = new UserInfo(null, guestId);

            assertThat(userInfo.isGuest()).isTrue();
            assertThat(userInfo.isMember()).isFalse();
            assertThat(userInfo.getGuestId()).isEqualTo(guestId);
        }

        @Test
        @DisplayName("memberId와 guestId가 동시에 null이면 예외 발생")
        void bothNull_throwsException() {
            assertThatThrownBy(() -> new UserInfo(null, null))
                    .isInstanceOf(UserInfoDomainException.class)
                    .hasMessageContaining("생성할 수 없습니다");
        }

        @Test
        @DisplayName("memberId와 guestId가 동시에 존재하면 예외 발생")
        void bothPresent_throwsException() {
            assertThatThrownBy(() -> new UserInfo(1L, "123e4567-e89b-12d3-a456-426614174000"))
                    .isInstanceOf(UserInfoDomainException.class)
                    .hasMessageContaining("동시에 지정할 수 없습니다");
        }

        @Test
        @DisplayName("잘못된 guestId 길이면 예외 발생")
        void invalidGuestId_throwsException() {
            assertThatThrownBy(() -> new UserInfo(null, "not-uuid-length"))
                    .isInstanceOf(UserInfoDomainException.class)
                    .hasMessageContaining("유효하지 않은 guestId");
        }
    }

    @Nested
    @DisplayName("잘못된 getter 호출 검증")
    class GetterTest {

        @Test
        @DisplayName("Member 타입에서 getGuestId 호출 시 예외 발생")
        void memberCallingGetGuestId_throwsException() {
            UserInfo userInfo = new UserInfo(1L, null);

            assertThatThrownBy(userInfo::getGuestId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("회원 컨텍스트");
        }

        @Test
        @DisplayName("Guest 타입에서 getMemberId 호출 시 예외 발생")
        void guestCallingGetMemberId_throwsException() {
            String guestId = "123e4567-e89b-12d3-a456-426614174000";
            UserInfo userInfo = new UserInfo(null, guestId);

            assertThatThrownBy(userInfo::getMemberId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("비회원 컨텍스트");
        }
    }
}
