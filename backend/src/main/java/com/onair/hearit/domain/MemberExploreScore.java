package com.onair.hearit.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_explore_score")
public class MemberExploreScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long hearitId;

    private Double score;

    private Long cursor_id;

    public MemberExploreScore(Long memberId, Long hearitId, Double score, Long cursor_id) {
        this.memberId = memberId;
        this.hearitId = hearitId;
        this.score = score;
        this.cursor_id = cursor_id;
    }
}
