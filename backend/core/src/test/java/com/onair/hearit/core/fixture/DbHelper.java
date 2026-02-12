package com.onair.hearit.core.fixture;

import com.onair.hearit.common.TestClock;
import com.onair.hearit.core.domain.Advertisement;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.ExploreScore;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitCluster;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Reaction;
import com.onair.hearit.core.domain.RecommendHearit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DbHelper {

    @PersistenceContext
    private EntityManager em;

    public Member insertMember(Member member) {
        em.persist(member);
        em.flush();
        return member;
    }

    public Hearit insertHearit(Hearit hearit) {
        em.persist(hearit);
        em.flush();
        return hearit;
    }

    public Hearit insertHearitAt(Hearit hearit, LocalDateTime creationTime) {
        try {
            TestClock.freezeAt(creationTime);
            return insertHearit(hearit);
        } finally {
            TestClock.unfreeze();
        }
    }

    public Category insertCategory(Category category) {
        em.persist(category);
        em.flush();
        return category;
    }

    public Bookmark insertBookmark(Bookmark bookmark) {
        em.persist(bookmark);
        em.flush();
        return bookmark;
    }

    public Keyword insertKeyword(Keyword keyword) {
        em.persist(keyword);
        em.flush();
        return keyword;
    }

    public HearitKeyword insertHearitKeyword(HearitKeyword hearitKeyword) {
        em.persist(hearitKeyword);
        em.flush();
        return hearitKeyword;
    }

    public RecommendHearit insertRecommendHearit(RecommendHearit recommendHearit) {
        em.persist(recommendHearit);
        em.flush();
        return recommendHearit;
    }

    public ExploreScore insertMemberExploreScore(ExploreScore exploreScore) {
        em.persist(exploreScore);
        em.flush();
        return exploreScore;
    }

    public PlayingHistory insertPlayingHistory(PlayingHistory playingHistory) {
        em.persist(playingHistory);
        em.flush();
        return playingHistory;
    }

    public PlayingHistory insertPlayingHistoryAt(PlayingHistory playingHistory, LocalDateTime updatedTime) {
        try {
            TestClock.freezeAt(updatedTime);
            return insertPlayingHistory(playingHistory);
        } finally {
            TestClock.unfreeze();
        }
    }

    public Reaction insertReaction(Reaction reaction) {
        em.persist(reaction);
        em.flush();
        return reaction;
    }

    public Advertisement insertAdvertisement(Advertisement advertisement) {
        em.persist(advertisement);
        em.flush();
        return advertisement;
    }

    public HearitCluster insertHearitCluster(HearitCluster hearitCluster) {
        em.persist(hearitCluster);
        em.flush();
        return hearitCluster;
    }
}
