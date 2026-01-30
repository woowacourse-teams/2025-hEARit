package com.onair.hearit.app.advertisement.application;

import com.onair.hearit.app.advertisement.dto.AdvertisementResponse;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.Advertisement;
import com.onair.hearit.core.infrastructure.jpa.AdvertisementRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    @Transactional(readOnly = true)
    public AdvertisementResponse getRandomAdvertisement() {
        List<Long> advertisementIds = advertisementRepository.findLatestIds();
        if (advertisementIds.isEmpty()) {
            log.error("등록된 광고가 존재하지 않습니다.");
            throw new IllegalStateException("등록된 광고가 존재하지 않습니다.");
        }
        Long randomId = pickRandomId(advertisementIds);
        Advertisement advertisement = advertisementRepository.findById(randomId)
                .orElseThrow(() -> new NotFoundException("advertisementId", randomId.toString()));
        return AdvertisementResponse.from(advertisement);
    }

    private Long pickRandomId(List<Long> ids) {
        List<Long> mutableIds = new ArrayList<>(ids);
        Collections.shuffle(mutableIds, new Random());
        return mutableIds.get(0);
    }
}
