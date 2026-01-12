package com.onair.hearit.app.advertisement.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.advertisement.dto.AdvertisementResponse;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.core.domain.Advertisement;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class AdvertisementIntegrationTest extends IntegrationTest {

    @Autowired
    DbHelper dbHelper;

    @Test
    @DisplayName("랜덤 광고 조회 성공 시 200 OK 및 광고 정보를 반환한다.")
    void getRandomAdvertisement_success() {
        // given
        Advertisement ad1 = dbHelper.insertAdvertisement(
                new Advertisement("https://example.com/image1.jpg", "https://example.com/link1", "광고1")
        );
        Advertisement ad2 = dbHelper.insertAdvertisement(
                new Advertisement("https://example.com/image2.jpg", "https://example.com/link2", "광고2")
        );

        // when
        AdvertisementResponse response = RestAssured.given(this.spec).log().all()
                .when()
                .get("/api/v1/advertisements/random")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(AdvertisementResponse.class);

        // then
        assertAll(
                () -> assertThat(response.id()).isIn(ad1.getId(), ad2.getId()),
                () -> assertThat(response.imageUrl()).isNotBlank(),
                () -> assertThat(response.linkUrl()).isNotBlank(),
                () -> assertThat(response.title()).isNotBlank()
        );
    }

    @Test
    @DisplayName("등록된 광고가 없으면 404 NOT FOUND를 반환한다.")
    void getRandomAdvertisement_notFound() {
        // when & then
        RestAssured.given(this.spec).log().all()
                .when()
                .get("/api/v1/advertisements/random")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
