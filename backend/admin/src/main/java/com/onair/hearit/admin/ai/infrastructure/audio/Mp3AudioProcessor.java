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

    public byte[] createShortClip(byte[] originalMp3) {
        return createShortClip(originalMp3, shortsDurationSeconds);
    }

    @Override
    public AudioMetadata extractMetadata(byte[] mp3Data) {
        double durationSeconds = audioClipper.getDuration(mp3Data, EXTENSION);
        int bitrate = audioClipper.getBitrate(mp3Data, EXTENSION);
        if (bitrate <= 0) {
            bitrate = DEFAULT_BITRATE_KBPS;
            log.warn("비트레이트 추출 실패, 기본값 사용: {}kbps", bitrate);
        }
        if (durationSeconds <= 0) {
            durationSeconds = (mp3Data.length * 8.0) / (bitrate * 1000);
            log.warn("재생시간 추출 실패, 추정값 사용: {}초", String.format("%.1f", durationSeconds));
        }

        log.debug("MP3 메타데이터: 비트레이트={}kbps, 재생시간={}초", bitrate, String.format("%.1f", durationSeconds));
        return new AudioMetadata(bitrate, durationSeconds);
    }

    @Override
    public void validate(byte[] data, String filename) {
        if (data == null || data.length == 0) {
            throw AudioProcessingException.invalidFile("오디오 데이터가 비어있습니다.");
        }
        if (data.length > MAX_FILE_SIZE_BYTES) {
            throw AudioProcessingException.fileTooLarge(
                    String.format("파일 크기가 %dMB를 초과합니다. (현재: %.1fMB)",
                            MAX_FILE_SIZE_MB, data.length / (1024.0 * 1024.0)));
        }
        if (filename == null || !filename.toLowerCase().endsWith(".mp3")) {
            throw AudioProcessingException.unsupportedFormat("MP3 파일만 지원합니다.");
        }
        if (!isValidMp3(data)) {
            throw AudioProcessingException.invalidFile("유효하지 않은 MP3 파일입니다.");
        }
    }

    private boolean isValidMp3(byte[] data) {
        if (data == null || data.length < 3) {
            return false;
        }
        if (data[0] == 'I' && data[1] == 'D' && data[2] == '3') {
            return true;
        }
        return (data[0] & 0xFF) == 0xFF && ((data[1] & 0xE0) == 0xE0);
    }
}
