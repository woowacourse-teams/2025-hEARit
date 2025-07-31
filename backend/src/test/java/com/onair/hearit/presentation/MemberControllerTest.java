package com.onair.hearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.MemberInfoResponse;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;


class MemberControllerTest extends IntegrationTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로그인한 사용자가 사용자 정보 조회 시, 200 OK 및 사용지 정보를 제공한다.")
    void getMemberInfo_success() {
        // given
        String socialId = "12345678";
        String nickname = "nickname";
        String profileImage = "profile-image.jpg";
        Member member = dbHelper.insertMember(Member.createSocialUser(socialId, nickname, profileImage));

        String token = generateToken(member);

        // when & then
        MemberInfoResponse response = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .filter(document("member-read-me",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("내 정보 조회")
                                .description("현재 로그인한 사용자의 정보를 조회합니다.")
                                .responseSchema(Schema.schema("MemberInfoResponse"))
                                .responseFields(
                                        fieldWithPath("id").description("사용자 ID"),
                                        fieldWithPath("nickname").description("닉네임"),
                                        fieldWithPath("profileImage").description("프로필 이미지 URL")
                                )
                                .build()))
                )
                .when()
                .get("/api/v1/members/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(MemberInfoResponse.class);

        assertAll(() -> {
            assertThat(response.id()).isEqualTo(member.getId());
            assertThat(response.nickname()).isEqualTo(member.getNickname());
            assertThat(response.profileImage()).isEqualTo(member.getProfileImage());
        });
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 사용자 정보 조회 시, 401 Unauthorized 예외를 반환한다.")
    void getMemberInfo_error_when_isNotLoginedMember() {
        RestAssured.given().log().all()
                .when()
                .get("/api/v1/members/me")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createToken(member.getId());
    }
}
