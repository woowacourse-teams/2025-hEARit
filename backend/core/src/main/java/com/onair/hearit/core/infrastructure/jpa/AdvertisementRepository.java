package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Advertisement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    @Query("SELECT a.id FROM Advertisement a")
    List<Long> findAllIds();
}
