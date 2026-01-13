package com.onair.hearit.app.advertisement.application;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.Advertisement;
import com.onair.hearit.core.infrastructure.jpa.AdvertisementRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    public Advertisement getRandomAdvertisement() {
        List<Long> advertisementIds = advertisementRepository.findAllIds();
        if (advertisementIds.isEmpty()) {
            throw new NotFoundException("advertisement", "전체");
        }
        Long randomId = pickRandomId(advertisementIds);
        return advertisementRepository.findById(randomId)
                .orElseThrow(() -> new NotFoundException("advertisementId", randomId.toString()));
    }

    private Long pickRandomId(List<Long> ids) {
        List<Long> mutableIds = new ArrayList<>(ids);
        Collections.shuffle(mutableIds, new Random());
        return mutableIds.get(0);
    }
}
