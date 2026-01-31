package com.onair.hearit.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.exception.UserInfoDomainException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserInfo 도메인 테스트")
class UserInfoTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTest {

        @Test
        @DisplayName("UUID와 MEMBER 타입으로 생성된다")
        void createWithMemberType() {
            UUID uuid = UUID.randomUUID();
            UserInfo userInfo = new UserInfo(uuid, UserType.MEMBER);

            assertAll(
                    () -> assertThat(userInfo.isMember()).isTrue(),
                    () -> assertThat(userInfo.isGuest()).isFalse(),
                    () -> assertThat(userInfo.getUuid()).isEqualTo(uuid),
                    () -> assertThat(userInfo.getUserType()).isEqualTo(UserType.MEMBER)
            );
        }

        @Test
        @DisplayName("UUID와 GUEST 타입으로 생성된다")
        void createWithGuestType() {
            UUID uuid = UUID.randomUUID();
            UserInfo userInfo = new UserInfo(uuid, UserType.GUEST);

            assertAll(
                    () -> assertThat(userInfo.isGuest()).isTrue(),
                    () -> assertThat(userInfo.isMember()).isFalse(),
                    () -> assertThat(userInfo.getUuid()).isEqualTo(uuid),
                    () -> assertThat(userInfo.getUserType()).isEqualTo(UserType.GUEST)
            );
        }

        @Test
        @DisplayName("uuid가 null이면 예외 발생")
        void nullUuid_throwsException() {
            assertThatThrownBy(() -> new UserInfo(null, UserType.MEMBER))
                    .isInstanceOf(UserInfoDomainException.class)
                    .hasMessageContaining("uuid는 null일 수 없습니다");
        }

        @Test
        @DisplayName("userType이 null이면 예외 발생")
        void nullUserType_throwsException() {
            UUID uuid = UUID.randomUUID();
            assertThatThrownBy(() -> new UserInfo(uuid, null))
                    .isInstanceOf(UserInfoDomainException.class)
                    .hasMessageContaining("userType은 null일 수 없습니다");
        }
    }
}
