package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.SeriesDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "series")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Series {

    public static final int TITLE_MAX_LENGTH = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final String IMAGE_KEY_PREFIX = "/series/image/";
    private static final String IMAGE_KEY_EXTENSION = ".jpg";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Series(String title, String description, String imageUrl) {
        validateTitle(title);
        validateDescription(description);
        validateImageKey(imageUrl);
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > TITLE_MAX_LENGTH) {
            throw new SeriesDomainException("시리즈 제목은 " + TITLE_MAX_LENGTH + "자 이하의 문자열이어야합니다.");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new SeriesDomainException("시리즈 설명은 " + DESCRIPTION_MAX_LENGTH + "자 이하의 문자열이어야합니다.");
        }
    }

    private void validateImageKey(String imageUrl) {
        if (imageUrl == null) {
            return;
        }
        if (!imageUrl.startsWith(IMAGE_KEY_PREFIX) || !imageUrl.endsWith(IMAGE_KEY_EXTENSION)) {
            throw new SeriesDomainException(
                    "시리즈 이미지 키는 '" + IMAGE_KEY_PREFIX + "'로 시작하고 '" + IMAGE_KEY_EXTENSION + "' 확장자여야 합니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Series series)) {
            return false;
        }
        if (this.id == null || series.id == null) {
            return false;
        }
        return Objects.equals(id, series.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
