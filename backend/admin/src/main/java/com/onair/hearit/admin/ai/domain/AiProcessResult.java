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

    // 원본 파일 정보
    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "original_file_key", length = 500)
    private String originalFileKey;

    // 생성된 파일들 (S3 temp 경로)
    @Column(name = "generated_org_key", length = 500)
    private String generatedOrgKey;

    @Column(name = "generated_shr_key", length = 500)
    private String generatedShrKey;

    @Column(name = "generated_scr_key", length = 500)
    private String generatedScrKey;

    // AI 생성 결과
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

    // 수정된 값 (사용자 입력)
    @Convert(converter = ScriptSegmentListConverter.class)
    @Column(name = "edited_script", columnDefinition = "JSON")
    private List<ScriptSegment> editedScript;

    @Column(name = "edited_title", length = 35)
    private String editedTitle;

    @Column(name = "edited_summary", columnDefinition = "TEXT")
    private String editedSummary;

    // 재생 시간 (초)
    @Column(name = "play_time")
    private Integer playTime;

    // 에러 정보
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // 타임스탬프
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // 확인 후 생성된 Hearit
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_hearit_id")
    private Hearit confirmedHearit;

    @Builder
    public AiProcessResult(String originalFileName, String originalFileKey) {
        this.status = ProcessStatus.PENDING;
        this.originalFileName = originalFileName;
        this.originalFileKey = originalFileKey;
    }

    // 상태 변경 메서드
    public void updateStatus(ProcessStatus status) {
        this.status = status;
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

    // 파일 키 설정
    public void setGeneratedFiles(String orgKey, String shrKey, String scrKey) {
        this.generatedOrgKey = orgKey;
        this.generatedShrKey = shrKey;
        this.generatedScrKey = scrKey;
    }

    // AI 결과 설정
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

    // 사용자 수정 저장
    public void updateEditedScript(List<ScriptSegment> editedScript) {
        this.editedScript = editedScript;
    }

    public void updateEditedMetadata(String title, String summary) {
        this.editedTitle = title;
        this.editedSummary = summary;
    }

    // 최종 값 반환 (edited가 있으면 edited, 없으면 suggested/corrected)
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
        return ProcessStatusUtil.getProgress(this.status);
    }

    public String getStatusMessage() {
        return ProcessStatusUtil.getStatusMessage(this.status);
    }
}
