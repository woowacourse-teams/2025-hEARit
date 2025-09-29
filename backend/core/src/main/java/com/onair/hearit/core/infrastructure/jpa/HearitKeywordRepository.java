package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HearitKeywordRepository extends JpaRepository<HearitKeyword, Long> {

    @Query("""
                SELECT hk
                FROM HearitKeyword hk
                JOIN FETCH hk.hearit h
                JOIN FETCH hk.keyword k
                WHERE hk.hearit.id IN :hearitIds
            """)
    List<HearitKeyword> findByHearitIdIn(@Param("hearitIds") List<Long> hearitIds);

    @Query("""
                SELECT k
                FROM HearitKeyword hk
                JOIN hk.keyword k
                WHERE hk.hearit.id = :hearitId
            """)
    List<Keyword> findKeywordsByHearitId(@Param("hearitId") Long hearitId);

    @Query("""
            SELECT hk
            FROM HearitKeyword hk
            JOIN FETCH hk.keyword
            WHERE hk.hearit IN :hearits""")
    List<HearitKeyword> findAllByHearitIn(@Param("hearits") List<Hearit> hearits);

    @Query("""
                SELECT k
                FROM HearitKeyword hk
                JOIN hk.keyword k
                WHERE hk.hearit.id = :hearitId
                ORDER BY hk.id DESC
                LIMIT :size
            """)
    List<Keyword> findRecentKeywordsByHearitId(@Param("hearitId") Long hearitId, @Param("size") int size);
}
