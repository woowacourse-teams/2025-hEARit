package com.onair.hearit.app.advertisement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.domain.Advertisement;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.AdvertisementRepository;
import java.util.HashSet;
import java.util.Set;
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
            Advertisement result = advertisementService.getRandomAdvertisement();

            // then
            assertThat(result).isIn(ad1, ad2, ad3);
        }

        @Test
        @DisplayName("여러 번 호출 시 다양한 광고가 선택된다.")
        void getRandomAdvertisement_randomness() {
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
            Set<Long> selectedIds = new HashSet<>();
            for (int i = 0; i < 30; i++) {
                Advertisement result = advertisementService.getRandomAdvertisement();
                selectedIds.add(result.getId());
            }

            // then
            assertThat(selectedIds.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("광고가 하나만 있어도 정상적으로 반환한다.")
        void getRandomAdvertisement_singleAd() {
            // given
            Advertisement ad = dbHelper.insertAdvertisement(
                    new Advertisement("https://example.com/image.jpg", "https://example.com/link", "광고")
            );

            // when
            Advertisement result = advertisementService.getRandomAdvertisement();

            // then
            assertThat(result).isEqualTo(ad);
        }

        @Test
        @DisplayName("등록된 광고가 없으면 예외가 발생한다.")
        void getRandomAdvertisement_noAds() {
            // when & then
            assertThatThrownBy(() -> advertisementService.getRandomAdvertisement())
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("advertisement을(를) 찾을 수 없습니다. 입력값: 전체");
        }
    }
}
