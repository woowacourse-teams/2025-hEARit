package com.onair.hearit.app.advertisement.application;

import com.onair.hearit.app.advertisement.dto.AdvertisementResponse;
import com.onair.hearit.app.common.RandomNumberGenerator;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.Advertisement;
import com.onair.hearit.core.infrastructure.jpa.AdvertisementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final RandomNumberGenerator randomNumberGenerator;

    @Transactional(readOnly = true)
    public AdvertisementResponse getRandomAdvertisement() {
        List<Long> advertisementIds = advertisementRepository.findLatestIds();
        if (advertisementIds.isEmpty()) {
            log.error("등록된 광고가 존재하지 않습니다.");
            throw new IllegalStateException("등록된 광고가 존재하지 않습니다.");
        }
        Long randomId = advertisementIds.get((int) (randomNumberGenerator.getDouble() * advertisementIds.size()));
        Advertisement advertisement = advertisementRepository.findById(randomId)
                .orElseThrow(() -> new NotFoundException("advertisementId", randomId.toString()));
        return AdvertisementResponse.from(advertisement);
    }
}
