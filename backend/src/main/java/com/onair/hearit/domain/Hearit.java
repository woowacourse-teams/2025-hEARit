package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "hearit")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hearit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary")
    private String summary;

    @Column(name = "play_time", nullable = false)
    private Integer playTime;

    @Column(name = "original_audio_url", nullable = false)
    private String originalAudioUrl;

    @Column(name = "short_audio_url", nullable = false)
    private String shortAudioUrl;

    @Column(name = "script_url", nullable = false)
    private String scriptUrl;

    @Column(name = "source")
    private String source;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Hearit(String title, String summary, Integer playTime, String originalAudioUrl,
                  String shortAudioUrl, String scriptUrl, String source, Category category) {
        validateMetaData(title, summary, playTime, source, category);
        this.title = title;
        this.summary = summary;
        this.playTime = playTime;
        this.originalAudioUrl = originalAudioUrl;
        this.shortAudioUrl = shortAudioUrl;
        this.scriptUrl = scriptUrl;
        this.source = source;
        this.category = category;
    }

    public void updateMetaData(String title, String summary, Integer playTime, String source, Category category) {
        validateMetaData(title, summary, playTime, source, category);
        this.title = title;
        this.summary = summary;
        this.playTime = playTime;
        this.source = source;
        this.category = category;
    }

    private void validateMetaData(String title, String summary, Integer playTime, String source, Category category) {
        if (isEmptyString(title) || title.length() > 35) {
            throw new InvalidInputException("제목은 35자 이하의 문자열이어야합니다.");
        }
        if (isEmptyString(summary) || summary.length() > 250) {
            throw new InvalidInputException("요약은 250자 이하의 문자열이어야합니다.");
        }
        if (playTime == null || playTime < 1) {
            throw new InvalidInputException("총 길이는 1초 이상의 숫자여야합니다.");
        }
        if (isEmptyString(source) || source.length() > 250) {
            throw new InvalidInputException("출처는 250자 이하의 문자열이어야합니다.");
        }
        if (category == null) {
            throw new InvalidInputException("카테고리는 반드시 입력해야합니다.");
        }
    }

    private boolean isEmptyString(String title) {
        return title == null || title.isBlank();
    }

    public void updateFileUrl(String fileUrl, FileType fileType) {
        fileType.updateFileUrl(this, fileUrl);
    }

    void updateOriginalAudioUrl(String originalAudioUrl) {
        this.originalAudioUrl = originalAudioUrl;
    }

    void updateShortAudioUrl(String shortAudioUrl) {
        this.shortAudioUrl = shortAudioUrl;
    }

    void updateScriptUrl(String scriptUrl) {
        this.scriptUrl = scriptUrl;
    }

    public String getFileUrl(FileType fileType) {
        return fileType.getFileUrl(this);
    }
}
