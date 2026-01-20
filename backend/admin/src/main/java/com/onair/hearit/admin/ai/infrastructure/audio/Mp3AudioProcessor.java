package com.onair.hearit.admin.ai.infrastructure.audio;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Mp3AudioProcessor implements AudioProcessor {

    private static final int MAX_FILE_SIZE_MB = 25;
    private static final int MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    private static final int DEFAULT_BITRATE_KBPS = 128;
    private static final String EXTENSION = "mp3";

    private final AudioClipper audioClipper;

    @Value("${ai.shorts.duration.seconds:60}")
    private int shortsDurationSeconds;

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".mp3");
    }

    @Override
    public String getExtension() {
        return "mp3";
    }

    @Override
    public String getMimeType() {
        return "audio/mpeg";
    }

    @Override
    public byte[] createShortClip(byte[] originalMp3, int durationSeconds) {
        log.info("MP3 쇼츠 생성 시작: 목표={}초", durationSeconds);
        return audioClipper.clip(originalMp3, EXTENSION, durationSeconds);
    }

    /**
     * 기본 쇼츠 길이로 쇼츠 생성
     */
    public byte[] createShortClip(byte[] originalMp3) {
        return createShortClip(originalMp3, shortsDurationSeconds);
    }

    @Override
    public AudioMetadata extractMetadata(byte[] mp3Data) {
        double durationSeconds = audioClipper.getDuration(mp3Data, EXTENSION);
        int bitrate = audioClipper.getBitrate(mp3Data, EXTENSION);

        // FFmpeg에서 추출 실패 시 기본값 사용
        if (bitrate <= 0) {
            bitrate = DEFAULT_BITRATE_KBPS;
            log.warn("비트레이트 추출 실패, 기본값 사용: {}kbps", bitrate);
        }
        if (durationSeconds <= 0) {
            durationSeconds = (mp3Data.length * 8.0) / (bitrate * 1000);
            log.warn("재생시간 추출 실패, 추정값 사용: {:.1f}초", durationSeconds);
        }

        log.debug("MP3 메타데이터: 비트레이트={}kbps, 재생시간={:.1f}초", bitrate, durationSeconds);
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
        if (filename == null || !filename.toLowerCase().endsWith(".mp3")) {
            throw AudioProcessingException.unsupportedFormat("MP3 파일만 지원합니다.");
        }

        // MP3 매직 바이트 검증
        if (!isValidMp3(data)) {
            throw AudioProcessingException.invalidFile("유효하지 않은 MP3 파일입니다.");
        }
    }

    /**
     * MP3 파일 형식 검증 (매직 바이트)
     */
    private boolean isValidMp3(byte[] data) {
        if (data == null || data.length < 3) {
            return false;
        }

        // ID3v2 태그 체크 ("ID3")
        if (data[0] == 'I' && data[1] == 'D' && data[2] == '3') {
            return true;
        }

        // MP3 프레임 싱크 워드 체크 (0xFF 0xFB, 0xFF 0xFA, 0xFF 0xF3, 0xFF 0xF2 등)
        // 첫 번째 바이트: 0xFF
        // 두 번째 바이트 상위 3비트: 0xE0 (111xxxxx)
        if ((data[0] & 0xFF) == 0xFF && ((data[1] & 0xE0) == 0xE0)) {
            return true;
        }

        return false;
    }
}
