package com.onair.hearit.admin.ai.infrastructure.audio;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * M4A/AAC 오디오 파일 처리기
 * FFmpeg(JAVE2)를 사용하여 정확한 시간 기반 자르기 수행
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class M4aAudioProcessor implements AudioProcessor {

    private static final int MAX_FILE_SIZE_MB = 25;
    private static final int MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    private static final int DEFAULT_BITRATE_KBPS = 128;
    private static final String EXTENSION = "m4a";

    private final AudioClipper audioClipper;

    @Value("${ai.shorts.duration.seconds:60}")
    private int shortsDurationSeconds;

    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".m4a") || lower.endsWith(".aac");
    }

    @Override
    public String getExtension() {
        return "m4a";
    }

    @Override
    public String getMimeType() {
        return "audio/mp4";
    }

    @Override
    public byte[] createShortClip(byte[] originalAudio, int durationSeconds) {
        if (originalAudio == null || originalAudio.length == 0) {
            throw new AudioProcessingException("오디오 데이터가 비어있습니다.");
        }

        log.info("M4A 쇼츠 생성 시작: 목표={}초", durationSeconds);
        return audioClipper.clip(originalAudio, EXTENSION, durationSeconds);
    }

    /**
     * 기본 쇼츠 길이로 쇼츠 생성
     */
    public byte[] createShortClip(byte[] originalAudio) {
        return createShortClip(originalAudio, shortsDurationSeconds);
    }

    @Override
    public AudioMetadata extractMetadata(byte[] audioData) {
        double durationSeconds = audioClipper.getDuration(audioData, EXTENSION);
        int bitrate = audioClipper.getBitrate(audioData, EXTENSION);

        // FFmpeg에서 추출 실패 시 기본값 사용
        if (bitrate <= 0) {
            bitrate = DEFAULT_BITRATE_KBPS;
            log.warn("비트레이트 추출 실패, 기본값 사용: {}kbps", bitrate);
        }
        if (durationSeconds <= 0) {
            durationSeconds = (audioData.length * 8.0) / (bitrate * 1000);
            log.warn("재생시간 추출 실패, 추정값 사용: {:.1f}초", durationSeconds);
        }

        log.debug("M4A 메타데이터: 비트레이트={}kbps, 재생시간={:.1f}초", bitrate, durationSeconds);
        return new AudioMetadata(bitrate, durationSeconds);
    }

    @Override
    public void validate(byte[] data, String filename) {
        // 파일 크기 검증 (25MB 제한 - Whisper API)
        if (data.length > MAX_FILE_SIZE_BYTES) {
            throw AudioProcessingException.fileTooLarge(
                    String.format("파일 크기가 %dMB를 초과합니다. (현재: %.1fMB)",
                            MAX_FILE_SIZE_MB, data.length / (1024.0 * 1024.0)));
        }

        // 확장자 검증
        if (filename == null || !supports(filename)) {
            throw AudioProcessingException.unsupportedFormat("M4A/AAC 파일만 지원합니다.");
        }

        // M4A 매직 바이트 검증
        if (!isValidM4a(data)) {
            throw AudioProcessingException.invalidFile("유효하지 않은 M4A 파일입니다.");
        }
    }

    /**
     * M4A 파일 형식 검증 (ftyp box 확인)
     * M4A 파일은 보통 ftyp box로 시작하며, offset 4-7에 "ftyp" 문자열이 있음
     */
    private boolean isValidM4a(byte[] data) {
        if (data == null || data.length < 8) {
            return false;
        }

        // ftyp box 확인만으로 MPEG-4 컨테이너 파일 검증
        return data[4] == 'f' && data[5] == 't' && data[6] == 'y' && data[7] == 'p';
    }
}
