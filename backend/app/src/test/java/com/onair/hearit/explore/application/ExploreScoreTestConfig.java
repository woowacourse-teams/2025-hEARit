package com.onair.hearit.explore.application;

import com.onair.hearit.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.explore.application.scorefactor.ScoreFactor;
import com.onair.hearit.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.infrastructure.jpa.HearitRepository;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ExploreScoreTestConfig {

    // 랜덤 값 대신 고정적인 1점을 주입
    @Bean
    public RandomScoreFactor randomScoreFactor() {
        return new RandomScoreFactor(() -> 0.1d);
    }

    @Bean
    public RecencyScoreFactor recencyScoreFactor() {
        return new RecencyScoreFactor();
    }

    @Bean
    public BookmarkScoreFactor bookmarkScoreFactor(MemberRepository memberRepository,
                                                   BookmarkRepository bookmarkRepository) {
        return new BookmarkScoreFactor(memberRepository, bookmarkRepository);
    }

    @Bean
    public ExploreScoreCalculator exploreScoreCalculator(
            HearitRepository hearitRepository,
            List<ScoreFactor> scoreFactors
    ) {
        return new ExploreScoreCalculator(hearitRepository, scoreFactors);
    }
}
