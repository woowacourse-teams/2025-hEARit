package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
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
import java.util.List;
import java.util.Objects;
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

    @Embedded
    private FileUrls fileUrls;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "hearit_source",
            joinColumns = @JoinColumn(name = "hearit_id")
    )
    private List<Source> sources;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Hearit(String title, String summary, Integer playTime, String originalAudioUrl,
                  String shortAudioUrl, String scriptUrl, List<Source> sources, Category category) {
        validateMetaData(title, summary, playTime, sources, category);
        this.title = title;
        this.summary = summary;
        this.playTime = playTime;
        this.fileUrls = new FileUrls(originalAudioUrl, shortAudioUrl, scriptUrl);
        this.sources = sources;
        this.category = category;
    }

    public void updateMetaData(String title, String summary, Integer playTime, List<Source> sources,
                               Category category) {
        validateMetaData(title, summary, playTime, sources, category);
        this.title = title;
        this.summary = summary;
        this.playTime = playTime;
        this.sources = sources;
        this.category = category;
    }

    private void validateMetaData(String title, String summary, Integer playTime, List<Source> sources,
                                  Category category) {
        validateTitle(title);
        validateSummary(summary);
        validatePlayTime(playTime);
        validateCategory(category);
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new InvalidInputException("카테고리는 반드시 입력해야합니다.");
        }
    }

    private void validatePlayTime(Integer playTime) {
        if (playTime == null || playTime < 1) {
            throw new InvalidInputException("총 길이는 1초 이상의 숫자여야합니다.");
        }
    }

    private void validateSummary(String summary) {
        if (isEmptyString(summary) || summary.length() > 250) {
            throw new InvalidInputException("요약은 250자 이하의 문자열이어야합니다.");
        }
    }

    private void validateTitle(String title) {
        if (isEmptyString(title) || title.length() > 35) {
            throw new InvalidInputException("제목은 35자 이하의 문자열이어야합니다.");
        }
    }

    private boolean isEmptyString(String title) {
        return title == null || title.isBlank();
    }

    public void updateFileUrl(String fileUrl, FileType fileType) {
        validateFile(fileUrl, fileType);
        this.fileUrls = fileType.updateFileUrls(this.fileUrls, fileUrl);
    }

    private void validateFile(String fileUrl, FileType fileType) {
        validateFileUrl(fileUrl);
        validateFileType(fileType);
    }

    private void validateFileType(FileType fileType) {
        if (fileType == null) {
            throw new InvalidInputException("파일 타입은 null이 될 수 없습니다.");
        }
    }

    private void validateFileUrl(String fileUrl) {
        if (fileUrl == null) {
            throw new InvalidInputException("파일 url은 null이 될 수 없습니다.");
        }
    }

    public String getFileUrl(FileType fileType) {
        validateFileType(fileType);
        return fileType.getFileUrls(this.fileUrls);
    }

    public String getOriginalAudioUrl() {
        return this.fileUrls.getOriginalAudioUrl();
    }

    public String getShortAudioUrl() {
        return this.fileUrls.getShortAudioUrl();
    }

    public String getScriptUrl() {
        return this.fileUrls.getScriptUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Hearit hearit)) {
            return false;
        }
        if (this.id == null || hearit.id == null) {
            return false;
        }
        return Objects.equals(id, hearit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
