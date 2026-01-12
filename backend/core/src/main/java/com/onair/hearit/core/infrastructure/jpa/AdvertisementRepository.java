package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    @Query(value = "SELECT * FROM advertisement ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Advertisement findRandomAd();
}
