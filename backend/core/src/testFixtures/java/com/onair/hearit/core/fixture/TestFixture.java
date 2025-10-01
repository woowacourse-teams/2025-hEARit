package com.onair.hearit.core.fixture;

import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.domain.UserInfo;
import java.util.List;
import java.util.UUID;

public class TestFixture {

    public static Member createFixedMember() {
        return Member.createLocalUser(
                UUID.randomUUID().toString(),
                "memberID",
                "nickname",
                "password",
                "profile-image.jpg");
    }

    public static UserInfo createFixedMemberUserInfo(Member member) {
        return new UserInfo(member.getId(), null);
    }

    public static UserInfo createFixedGuestUserInfo(String guestId) {
        return new UserInfo(null, guestId);
    }

    public static Keyword createFixedKeyword() {
        return new Keyword("AI");
    }

    public static Category createFixedCategory() {
        return new Category("Spring", "#000000");
    }

    public static Category createCategoryByName(String name) {
        return new Category(name, "#000000");
    }

    public static Hearit createFixedHearitWith(Category category) {
        return new Hearit(
                "title",
                "summary",
                500,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(
                        new Source("이 컨텐츠는 쿠버네티스 공식 문서 (저작자: The Kubernetes Authors)를 참고하여 만들어졌습니다.",
                                "https://example.com/1"),
                        new Source("원본은 CC BY 4.0 라이선스를 따릅니다.", "https://example.com/2")
                ),
                category);
    }

    public static Bookmark createFixedBookmark(Member member, Hearit hearit) {
        return new Bookmark(member, hearit);
    }

    public static List<Source> createFixedSources() {
        return List.of(
                new Source("이 컨텐츠는 쿠버네티스 공식 문서 (저작자: The Kubernetes Authors)를 참고하여 만들어졌습니다.",
                        "https://example.com/1"),
                new Source("원본은 CC BY 4.0 라이선스를 따릅니다.", "https://example.com/2")
        );
    }
}
