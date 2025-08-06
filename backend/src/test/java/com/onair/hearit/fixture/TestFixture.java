package com.onair.hearit.fixture;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.Source;
import java.util.List;

public class TestFixture {

    public static Member createFixedMember() {
        return Member.createLocalUser(
                "memberID",
                "nickname",
                "password",
                "profile-image.jpg");
    }

    public static Keyword createFixedKeyword() {
        return new Keyword("AI");
    }

    public static Category createFixedCategory() {
        return new Category("Spring", "#000000");
    }

    public static Hearit createFixedHearitWith(Category category) {
        return new Hearit(
                "title",
                "summary",
                500,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                createFixedSources(),
                category);
    }

    public static Bookmark createFixedBookmark(Member member, Hearit hearit) {
        return new Bookmark(member, hearit);
    }

    public static List<Source> createFixedSources() {
        return List.of(
                new Source("출처1", "https://example.com/1"),
                new Source("출처2", "https://example.com/2")
        );
    }
}
