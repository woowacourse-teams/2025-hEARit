package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Keyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    @Query(
            value = "SELECT * FROM keyword ORDER BY RAND(:seed) LIMIT :size",
            nativeQuery = true
    )
    List<Keyword> findRandomWithSeed(@Param("seed") long seed, @Param("size") int size);
}
