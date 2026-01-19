package com.onair.hearit.admin.ai.domain;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.core.domain.Hearit;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "ai_process_result")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiProcessResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessStatus status;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "original_file_key", length = 500)
    private String originalFileKey;

    @Column(name = "generated_org_key", length = 500)
    private String generatedOrgKey;

    @Column(name = "generated_shr_key", length = 500)
    private String generatedShrKey;

    @Column(name = "generated_scr_key", length = 500)
    private String generatedScrKey;

    @Convert(converter = ScriptSegmentListConverter.class)
    @Column(name = "raw_transcript", columnDefinition = "JSON")
    private List<ScriptSegment> rawTranscript;

    @Convert(converter = ScriptSegmentListConverter.class)
    @Column(name = "corrected_script", columnDefinition = "JSON")
    private List<ScriptSegment> correctedScript;

    @Column(name = "suggested_title", length = 35)
    private String suggestedTitle;

    @Column(name = "suggested_summary", columnDefinition = "TEXT")
    private String suggestedSummary;

    @Convert(converter = ScriptSegmentListConverter.class)
    @Column(name = "edited_script", columnDefinition = "JSON")
    private List<ScriptSegment> editedScript;

    @Column(name = "edited_title", length = 35)
    private String editedTitle;

    @Column(name = "edited_summary", columnDefinition = "TEXT")
    private String editedSummary;

    @Column(name = "play_time")
    private Integer playTime;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_hearit_id")
    private Hearit confirmedHearit;

    @Builder
    public AiProcessResult(String originalFileName, String originalFileKey) {
        this.status = ProcessStatus.PENDING;
        this.originalFileName = originalFileName;
        this.originalFileKey = originalFileKey;
    }

    public void markAsUploading() {
        this.status = ProcessStatus.UPLOADING;
    }

    public void markAsConverting() {
        this.status = ProcessStatus.CONVERTING;
    }

    public void markAsTranscribing() {
        this.status = ProcessStatus.TRANSCRIBING;
    }

    public void markAsCorrecting() {
        this.status = ProcessStatus.CORRECTING;
    }

    public void markAsGeneratingMeta() {
        this.status = ProcessStatus.GENERATING_META;
    }

    public void markAsCompleted(LocalDateTime expiresAt) {
        this.status = ProcessStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public void markAsFailed(String errorMessage) {
        this.status = ProcessStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsConfirmed(Hearit hearit) {
        this.status = ProcessStatus.CONFIRMED;
        this.confirmedHearit = hearit;
    }

    public void setGeneratedFiles(String orgKey, String shrKey, String scrKey) {
        this.generatedOrgKey = orgKey;
        this.generatedShrKey = shrKey;
        this.generatedScrKey = scrKey;
    }

    public void setTranscriptionResult(List<ScriptSegment> rawTranscript, int playTime) {
        this.rawTranscript = rawTranscript;
        this.playTime = playTime;
    }

    public void setCorrectedScript(List<ScriptSegment> correctedScript) {
        this.correctedScript = correctedScript;
    }

    public void setSuggestedMetadata(String title, String summary) {
        this.suggestedTitle = title;
        this.suggestedSummary = summary;
    }

    public void updateEditedScript(List<ScriptSegment> editedScript) {
        this.editedScript = editedScript;
    }

    public void updateEditedMetadata(String title, String summary) {
        this.editedTitle = title;
        this.editedSummary = summary;
    }

    public List<ScriptSegment> getFinalScript() {
        return editedScript != null ? editedScript : correctedScript;
    }

    public String getFinalTitle() {
        return editedTitle != null ? editedTitle : suggestedTitle;
    }

    public String getFinalSummary() {
        return editedSummary != null ? editedSummary : suggestedSummary;
    }

    public int getProgress() {
        return this.status.getProgress();
    }

    public String getStatusMessage() {
        return this.status.getMessage();
    }
}
