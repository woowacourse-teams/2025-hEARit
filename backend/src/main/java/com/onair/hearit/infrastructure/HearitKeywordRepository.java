package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.HearitKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HearitKeywordRepository extends JpaRepository<HearitKeyword, Long> {

    @Query("""
                SELECT hk
                FROM HearitKeyword hk
                JOIN hk.keyword k
                WHERE hk.hearit.id IN :hearitIds
            """)
    List<HearitKeyword> findByHearitIdIn(@Param("hearitIds") List<Long> hearitIds);

}
