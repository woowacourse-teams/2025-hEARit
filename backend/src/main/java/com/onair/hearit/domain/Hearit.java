package com.onair.hearit.domain;

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

    public Hearit(String title, String summary, Integer playTime, String originalAudioUrl, String shortAudioUrl,
                  String scriptUrl, String source, LocalDateTime createdAt, Category category) {
        this.title = title;
        this.summary = summary;
        this.playTime = playTime;
        this.originalAudioUrl = originalAudioUrl;
        this.shortAudioUrl = shortAudioUrl;
        this.scriptUrl = scriptUrl;
        this.source = source;
        this.createdAt = createdAt;
        this.category = category;
    }

    public Hearit(String title, String summary, Integer playTime, String originalAudioUrl,
                  String shortAudioUrl, String scriptUrl, String source, Category category) {
        this.title = title;
        this.summary = summary;
        this.playTime = playTime;
        this.originalAudioUrl = originalAudioUrl;
        this.shortAudioUrl = shortAudioUrl;
        this.scriptUrl = scriptUrl;
        this.source = source;
        this.category = category;
    }

    public void update(String title, String summary, Integer playTime, String originalAudioUrl,
                       String shortAudioUrl, String scriptUrl, String source, Category category) {
        this.title = title;
        this.summary = summary;
        this.playTime = playTime;
        this.originalAudioUrl = originalAudioUrl;
        this.shortAudioUrl = shortAudioUrl;
        this.scriptUrl = scriptUrl;
        this.source = source;
        this.category = category;
    }
}
