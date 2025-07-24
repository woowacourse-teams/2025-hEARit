package com.onair.hearit.fixture;

import com.onair.hearit.domain.Member;

public class TestFixture {

    public static Member createFixedMember() {
        return Member.createLocalUser("memberID", "nickname", "password", "profile-image.jpg");
    }
}
