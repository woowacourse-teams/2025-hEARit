package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.AdvertisementDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "advertisement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Advertisement {

    public static final int TITLE_MAX_LENGTH = 35;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "link_url", nullable = false)
    private String linkUrl;

    @Column(name = "title", nullable = false)
    private String title;

    public Advertisement(String imageUrl, String linkUrl, String title) {
        validate(imageUrl, linkUrl, title);
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.title = title;
    }

    private void validate(String imageUrl, String linkUrl, String title) {
        validateImageUrl(imageUrl);
        validateLinkUrl(linkUrl);
        validateTitle(title);
    }

    private void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new AdvertisementDomainException("이미지 URL은 필수입니다.");
        }
    }

    private void validateLinkUrl(String linkUrl) {
        if (linkUrl == null || linkUrl.isBlank()) {
            throw new AdvertisementDomainException("링크 URL은 필수입니다.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new AdvertisementDomainException("제목은 필수입니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new AdvertisementDomainException("제목은 " + TITLE_MAX_LENGTH + "자 이하여야 합니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Advertisement advertisement)) {
            return false;
        }
        if (this.id == null || advertisement.id == null) {
            return false;
        }
        return Objects.equals(id, advertisement.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
