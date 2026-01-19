package com.onair.hearit.admin.ai.infrastructure.audio;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Mp3AudioProcessor {

    private static final int MAX_FILE_SIZE_MB = 25;
    private static final int MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

    private final int shortsDurationSeconds;

    public Mp3AudioProcessor(
            @Value("${ai.shorts.duration.seconds:60}") int shortsDurationSeconds) {
        this.shortsDurationSeconds = shortsDurationSeconds;
    }

    /**
     * MP3 파일에서 앞부분 N초를 잘라 쇼츠 생성
     *
     * @param originalMp3 원본 MP3 바이트 배열
     * @param durationSeconds 자를 길이 (초)
     * @return 잘린 MP3 바이트 배열
     */
    public byte[] createShortClip(byte[] originalMp3, int durationSeconds) {
        try {
            Mp3Metadata metadata = extractMetadata(originalMp3);

            // 원본이 목표 시간보다 짧으면 전체 반환
            if (metadata.getDurationSeconds() <= durationSeconds) {
                log.info("원본 오디오({:.1f}초)가 목표 시간({}초)보다 짧아 전체 반환",
                        metadata.getDurationSeconds(), durationSeconds);
                return originalMp3;
            }

            // 바이트/초 계산
            int bytesPerSecond = (metadata.getBitrate() * 1000) / 8;
            int targetBytes = bytesPerSecond * durationSeconds;

            log.info("쇼츠 생성: 비트레이트={}kbps, 목표={}초, 예상크기={}KB",
                    metadata.getBitrate(), durationSeconds, targetBytes / 1024);

            // 앞부분만 자르기
            return Arrays.copyOf(originalMp3, Math.min(targetBytes, originalMp3.length));

        } catch (AudioProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("쇼츠 생성 실패", e);
            throw new AudioProcessingException("쇼츠 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기본 쇼츠 길이로 쇼츠 생성
     */
    public byte[] createShortClip(byte[] originalMp3) {
        return createShortClip(originalMp3, shortsDurationSeconds);
    }

    /**
     * MP3 메타데이터 추출 (mp3spi 라이브러리 사용)
     */
    public Mp3Metadata extractMetadata(byte[] mp3Data) {
        try {
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(
                    new ByteArrayInputStream(mp3Data));

            Map<String, Object> properties = fileFormat.properties();

            // 비트레이트 추출 (bps → kbps)
            Object bitrateObj = properties.get("mp3.bitrate.nominal.bps");
            int bitrate;
            if (bitrateObj != null) {
                bitrate = ((Number) bitrateObj).intValue() / 1000;
            } else {
                // 비트레이트를 못 가져오면 파일 크기로 추정
                bitrate = estimateBitrate(mp3Data);
                log.warn("비트레이트 정보 없음, 추정값 사용: {}kbps", bitrate);
            }

            // 재생 시간 추출 (마이크로초 → 초)
            Object durationObj = properties.get("duration");
            double durationSeconds;
            if (durationObj != null) {
                long durationMicros = ((Number) durationObj).longValue();
                durationSeconds = durationMicros / 1_000_000.0;
            } else {
                // duration을 못 가져오면 파일 크기로 계산
                durationSeconds = (mp3Data.length * 8.0) / (bitrate * 1000);
                log.warn("재생시간 정보 없음, 추정값 사용: {:.1f}초", durationSeconds);
            }

            log.debug("MP3 메타데이터: 비트레이트={}kbps, 재생시간={:.1f}초", bitrate, durationSeconds);

            return new Mp3Metadata(bitrate, durationSeconds);

        } catch (UnsupportedAudioFileException e) {
            log.error("지원하지 않는 오디오 형식", e);
            throw AudioProcessingException.unsupportedFormat("MP3 파일만 지원합니다.");
        } catch (IOException e) {
            log.error("MP3 메타데이터 추출 실패", e);
            throw new AudioProcessingException("MP3 메타데이터 추출 실패", e);
        }
    }

    /**
     * 파일 크기로 비트레이트 추정 (일반적인 MP3는 5-20분 정도)
     */
    private int estimateBitrate(byte[] mp3Data) {
        // 1MB당 약 1분 = 128kbps 기준
        // 일반적인 팟캐스트는 128-192kbps
        return 128;
    }

    /**
     * MP3 파일 유효성 검증
     */
    public void validateMp3(byte[] data, String filename) {
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

    /**
     * MP3 메타데이터 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class Mp3Metadata {
        private final int bitrate;           // kbps
        private final double durationSeconds; // 재생 시간 (초)

        public int getDurationSecondsInt() {
            return (int) Math.ceil(durationSeconds);
        }
    }
}
