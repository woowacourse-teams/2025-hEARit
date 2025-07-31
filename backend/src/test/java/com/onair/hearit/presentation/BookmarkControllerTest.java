package com.onair.hearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.docs.ApiDocSnippets;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.BookmarkInfoResponse;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import io.restassured.RestAssured;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.FieldDescriptor;

class BookmarkControllerTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로그인한 사용자가 북마크 목록 조회 시, 200 OK 및 페이지에 따른 북마크 목록을 반환한다.")
    void readBookmarkHearitsTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        String token = generateToken(member);
        int bookmarkCount = 30;
        for (int i = 0; i < bookmarkCount; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
        }

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .param("page", 0)
                .param("size", 5)
                .filter(document("bookmark-read-list",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 목록 조회")
                                .description("사용자가 북마크한 히어릿 목록을 페이지별로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)")
                                )
                                .responseSchema(Schema.schema("PagedBookmarkHearitResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].hearitId").description("히어릿 ID"),
                                                        fieldWithPath("content[].bookmarkId").description("북마크 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].summary").description("히어릿 요약"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)")
                                                }),
                                                Arrays.stream(ApiDocSnippets.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ))
                .when()
                .get("/api/v1/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", equalTo(5));
    }

    @Test
    @DisplayName("로그인한 사용자가 북마크 목록 조회 시, page가 0 미만인 경우 400 BADREQUEST가 발생한다.")
    void readBookmarkHearitsTestWithBadRequestByPage() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .param("page", -1)
                .filter(document("bookmark-read-list-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 목록 조회")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    @DisplayName("로그인한 사용자가 북마크 목록 조회 시, size가 0 ~ 100이 아닌 경우 400 BADREQUEST가 발생한다.")
    void readBookmarkHearitsTestWithBadRequestBySize(int size) {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .param("size", size)
                .when()
                .get("/api/v1/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 북마크 목록 조회 시, 401 UNAUTHORIZED가 발생한다.")
    void readBookmarkHearits_error_401_whenNotLogin() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        int bookmarkCount = 10;
        for (int i = 0; i < bookmarkCount; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
        }

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "")
                .param("page", 0)
                .param("size", 20)
                .filter(document("bookmark-read-list-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 목록 조회")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("로그인한 사용자가 북마크 추가 시, 추가 후 201 CREATED를 반환한다.")
    void createBookmarkTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when & then
        BookmarkInfoResponse response = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .filter(document("bookmark-create",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 생성")
                                .description("로그인한 회원이 히어릿 ID로 북마크를 생성합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("북마크할 히어릿의 ID")
                                )
                                .responseSchema(Schema.schema("BookmarkInfoResponse"))
                                .responseFields(
                                        fieldWithPath("id").description("생성된 북마크의 ID")
                                )
                                .build())
                ))
                .when()
                .post("/api/v1/bookmarks/hearits/{hearitId}", hearit.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(BookmarkInfoResponse.class);

        assertThat(response.id()).isNotNull();
    }

    @Test
    @DisplayName("로그인한 사용자가 이미 추가된 북마크 추가 시, 추가 후 409 CONFLICT를 반환한다.")
    void createBookmarkTestWithConflict() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .filter(document("bookmark-create",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 생성")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .post("/api/v1/bookmarks/hearits/" + hearit.getId())
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("로그인한 사용자가 북마크 삭제 시, 삭제 후 204 NOCONTENT를 반환한다.")
    void deleteBookmark() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .filter(document("bookmark-delete",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 삭제")
                                .description("로그인한 히어릿 ID와 북마크 ID로 북마크를 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("bookmarkId").description("삭제할 북마크의 ID")
                                )
                                .build())
                ))
                .when()
                .delete("/api/v1/bookmarks/{bookmarkId}", bookmark.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("자신의 북마크가 아닌 북마크 삭제 시, 401 UNAUTHORIZED를 반환한다.")
    void notFoundHearitId() {
        // given
        Member bookmarkMember = dbHelper.insertMember(TestFixture.createFixedMember());
        Member notBookmarkMember = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(notBookmarkMember);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(bookmarkMember, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .filter(document("bookmark-delete-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 삭제")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .delete("/api/v1/bookmarks/" + bookmark.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createToken(member.getId());
    }
}
