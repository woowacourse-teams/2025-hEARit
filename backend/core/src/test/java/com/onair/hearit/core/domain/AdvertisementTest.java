package com.onair.hearit.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.exception.AdvertisementDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdvertisementTest {

    @Nested
    @DisplayName("광고 생성")
    class CreateAdvertisement {

        @Test
        @DisplayName("유효한 정보로 광고를 생성할 수 있다.")
        void create_success() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            String linkUrl = "https://example.com/link";
            String title = "광고 제목";

            // when
            Advertisement advertisement = new Advertisement(imageUrl, linkUrl, title);

            // then
            assertAll(
                    () -> assertThat(advertisement.getImageUrl()).isEqualTo(imageUrl),
                    () -> assertThat(advertisement.getLinkUrl()).isEqualTo(linkUrl),
                    () -> assertThat(advertisement.getTitle()).isEqualTo(title)
            );
        }

        @Test
        @DisplayName("이미지 URL이 null이면 예외가 발생한다.")
        void imageUrl_null() {
            // given
            String imageUrl = null;
            String linkUrl = "https://example.com/link";
            String title = "광고 제목";

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("이미지 URL은 필수입니다.");
        }

        @Test
        @DisplayName("이미지 URL이 빈 문자열이면 예외가 발생한다.")
        void imageUrl_blank() {
            // given
            String imageUrl = "   ";
            String linkUrl = "https://example.com/link";
            String title = "광고 제목";

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("이미지 URL은 필수입니다.");
        }

        @Test
        @DisplayName("이미지 URL이 최대 길이를 초과하면 예외가 발생한다.")
        void imageUrl_tooLong() {
            // given
            String imageUrl = "a".repeat(Advertisement.IMAGE_URL_MAX_LENGTH + 1);
            String linkUrl = "https://example.com/link";
            String title = "광고 제목";

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("이미지 URL은 " + Advertisement.IMAGE_URL_MAX_LENGTH + "자 이하여야 합니다.");
        }

        @Test
        @DisplayName("링크 URL이 null이면 예외가 발생한다.")
        void linkUrl_null() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            String linkUrl = null;
            String title = "광고 제목";

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("링크 URL은 필수입니다.");
        }

        @Test
        @DisplayName("링크 URL이 빈 문자열이면 예외가 발생한다.")
        void linkUrl_blank() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            String linkUrl = "   ";
            String title = "광고 제목";

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("링크 URL은 필수입니다.");
        }

        @Test
        @DisplayName("링크 URL이 최대 길이를 초과하면 예외가 발생한다.")
        void linkUrl_tooLong() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            String linkUrl = "a".repeat(Advertisement.LINK_URL_MAX_LENGTH + 1);
            String title = "광고 제목";

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("링크 URL은 " + Advertisement.LINK_URL_MAX_LENGTH + "자 이하여야 합니다.");
        }

        @Test
        @DisplayName("제목이 null이면 예외가 발생한다.")
        void title_null() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            String linkUrl = "https://example.com/link";
            String title = null;

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("제목은 필수입니다.");
        }

        @Test
        @DisplayName("제목이 빈 문자열이면 예외가 발생한다.")
        void title_blank() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            String linkUrl = "https://example.com/link";
            String title = "   ";

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("제목은 필수입니다.");
        }

        @Test
        @DisplayName("제목이 최대 길이를 초과하면 예외가 발생한다.")
        void title_tooLong() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            String linkUrl = "https://example.com/link";
            String title = "a".repeat(Advertisement.TITLE_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> new Advertisement(imageUrl, linkUrl, title))
                    .isInstanceOf(AdvertisementDomainException.class)
                    .hasMessage("제목은 " + Advertisement.TITLE_MAX_LENGTH + "자 이하여야 합니다.");
        }
    }

    @Nested
    @DisplayName("광고 동등성")
    class Equality {

        @Test
        @DisplayName("ID가 같으면 동일한 광고로 판단한다.")
        void equals_sameId() {
            // given
            Advertisement ad1 = new Advertisement("url1", "link1", "title1");
            Advertisement ad2 = new Advertisement("url2", "link2", "title2");

            // when & then
            assertThat(ad1).isEqualTo(ad1);
        }

        @Test
        @DisplayName("ID가 null이면 동일한 광고로 판단하지 않는다.")
        void equals_nullId() {
            // given
            Advertisement ad1 = new Advertisement("url1", "link1", "title1");
            Advertisement ad2 = new Advertisement("url2", "link2", "title2");

            // when & then
            assertThat(ad1).isNotEqualTo(ad2);
        }
    }
}
