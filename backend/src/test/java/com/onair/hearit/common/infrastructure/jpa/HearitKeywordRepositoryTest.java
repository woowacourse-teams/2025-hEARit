package com.onair.hearit.common.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.HearitKeyword;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class HearitKeywordRepositoryTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Test
    @DisplayName("히어릿 아이디로 히어릿 키워드를 조회할 수 있다.")
    void findKeywordsByHearitId() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        HearitKeyword hearitKeyword = dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword));

        // when
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId());

        // then
        assertAll(
                () -> assertThat(keywords).hasSize(1),
                () -> assertThat(keywords.getFirst().getId()).isEqualTo(keyword.getId())
        );
    }

    @Test
    @DisplayName("히어릿 아이디로 원하는 개수만큼의 히어릿 키워드를 조회할 수 있다.")
    void findRecentKeywordsByHearitIdWithSize() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        Keyword keyword1 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword2 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword3 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        HearitKeyword hearitKeyword1 = dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword1));
        HearitKeyword hearitKeyword2 = dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword2));
        HearitKeyword hearitKeyword3 = dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword3));

        // when
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(hearit.getId(), 2);

        // then
        assertThat(keywords).hasSize(2);
    }
}
