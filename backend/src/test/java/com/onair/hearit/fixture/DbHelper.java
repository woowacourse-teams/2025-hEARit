package com.onair.hearit.fixture;

import com.onair.hearit.admin.domain.Admin;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.ExploreScore;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.HearitKeyword;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.RecommendHearit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    public Admin insertAdmin(Admin admin) {
        em.persist(admin);
        em.flush();
        return admin;
    }

    public Hearit insertHearit(Hearit hearit) {
        em.persist(hearit);
        em.flush();
        return hearit;
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
}
