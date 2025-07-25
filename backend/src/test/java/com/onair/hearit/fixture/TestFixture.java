package com.onair.hearit.fixture;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;

public class TestFixture {

    public static Member createFixedMember() {
        return Member.createLocalUser(
                "memberID",
                "nickname",
                "password",
                "profile-image.jpg");
    }

    public static Keyword createFixedKeyword() {
        return new Keyword("name");
    }

    public static Category createFixedCategory() {
        return new Category("name", "colorCode");
    }

    public static Hearit createFixedHearitWith(Category category) {
        return new Hearit(
                "title",
                "summary",
                500,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                "source",
                category);
    }

    public static Bookmark createFixedBookmark(Member member, Hearit hearit) {
        return new Bookmark(member, hearit);
    }
}
