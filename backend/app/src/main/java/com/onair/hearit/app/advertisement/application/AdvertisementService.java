package com.onair.hearit.app.advertisement.application;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.Advertisement;
import com.onair.hearit.core.infrastructure.jpa.AdvertisementRepository;
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
    private final Random random = new Random();

    public Advertisement getRandomAdvertisement() {
        List<Advertisement> advertisements = advertisementRepository.findAllAdvertisements();

        if (advertisements.isEmpty()) {
            throw new NotFoundException("등록된 광고가 없습니다.");
        }

        int randomIndex = random.nextInt(advertisements.size());
        return advertisements.get(randomIndex);
    }
}
