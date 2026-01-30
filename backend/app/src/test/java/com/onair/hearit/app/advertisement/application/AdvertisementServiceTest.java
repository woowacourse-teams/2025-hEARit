package com.onair.hearit.app.advertisement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.app.advertisement.dto.AdvertisementResponse;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.domain.Advertisement;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.AdvertisementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class AdvertisementServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    private AdvertisementService advertisementService;

    @BeforeEach
    void setup() {
        advertisementService = new AdvertisementService(advertisementRepository);
    }

    @Nested
    @DisplayName("랜덤 광고 조회")
    class GetRandomAdvertisement {

        @Test
        @DisplayName("등록된 광고가 있으면 랜덤으로 하나를 반환한다.")
        void getRandomAdvertisement_success() {
            // given
            Advertisement ad1 = dbHelper.insertAdvertisement(
                    new Advertisement("https://example.com/image1.jpg", "https://example.com/link1", "광고1")
            );
            Advertisement ad2 = dbHelper.insertAdvertisement(
                    new Advertisement("https://example.com/image2.jpg", "https://example.com/link2", "광고2")
            );
            Advertisement ad3 = dbHelper.insertAdvertisement(
                    new Advertisement("https://example.com/image3.jpg", "https://example.com/link3", "광고3")
            );

            // when
            AdvertisementResponse result = advertisementService.getRandomAdvertisement();

            // then
            assertThat(result.id()).isIn(ad1.getId(), ad2.getId(), ad3.getId());
        }

        @Test
        @DisplayName("등록된 광고가 없으면 IllegalStateException이 발생한다.")
        void getRandomAdvertisement_noAds() {
            // when & then
            assertThatThrownBy(() -> advertisementService.getRandomAdvertisement())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("등록된 광고가 존재하지 않습니다.");
        }
    }
}
