package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Keyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    @Query("SELECT k.id FROM Keyword k")
    List<Long> findAllIds();

    @Query("SELECT k FROM Keyword k WHERE k.id IN :ids")
    List<Keyword> findAllByIdIn(@Param("ids") List<Long> ids);
}
