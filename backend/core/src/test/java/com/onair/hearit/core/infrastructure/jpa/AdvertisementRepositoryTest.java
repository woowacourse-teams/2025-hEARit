package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.core.domain.Advertisement;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.util.List;
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
class AdvertisementRepositoryTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Nested
    @DisplayName("최신 광고 ID 목록 조회")
    class FindLatestIds {

        @Test
        @DisplayName("광고가 없으면 빈 리스트를 반환한다.")
        void findLatestIds_empty() {
            // when
            List<Long> result = advertisementRepository.findLatestIds();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("광고가 있으면 ID를 내림차순으로 반환한다.")
        void findLatestIds_orderByIdDesc() {
            // given
            Advertisement ad1 = dbHelper.insertAdvertisement(
                    new Advertisement("https://example.com/img1.jpg", "https://example.com/link1", "광고1"));
            Advertisement ad2 = dbHelper.insertAdvertisement(
                    new Advertisement("https://example.com/img2.jpg", "https://example.com/link2", "광고2"));
            Advertisement ad3 = dbHelper.insertAdvertisement(
                    new Advertisement("https://example.com/img3.jpg", "https://example.com/link3", "광고3"));

            // when
            List<Long> result = advertisementRepository.findLatestIds();

            // then
            assertThat(result).containsExactly(ad3.getId(), ad2.getId(), ad1.getId());
        }

        @Test
        @DisplayName("광고가 100개를 초과하면 최신 100개만 반환한다.")
        void findLatestIds_limit100() {
            // given
            for (int i = 0; i < 101; i++) {
                dbHelper.insertAdvertisement(
                        new Advertisement("https://example.com/img.jpg", "https://example.com/link", "광고" + i));
            }

            // when
            List<Long> result = advertisementRepository.findLatestIds();

            // then
            assertThat(result).hasSize(100);
        }
    }
}
