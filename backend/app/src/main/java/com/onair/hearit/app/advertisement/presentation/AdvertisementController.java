package com.onair.hearit.app.advertisement.presentation;

import com.onair.hearit.app.advertisement.application.AdvertisementService;
import com.onair.hearit.app.advertisement.dto.AdvertisementResponse;
import com.onair.hearit.core.domain.Advertisement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping("/random")
    public ResponseEntity<AdvertisementResponse> getRandomAdvertisement() {
        Advertisement advertisement = advertisementService.getRandomAdvertisement();
        AdvertisementResponse response = AdvertisementResponse.from(advertisement);
        return ResponseEntity.ok(response);
    }
}
