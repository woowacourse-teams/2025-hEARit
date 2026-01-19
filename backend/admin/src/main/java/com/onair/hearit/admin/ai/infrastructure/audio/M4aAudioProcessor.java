package com.onair.hearit.admin.ai.infrastructure.audio;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * M4A/AAC 오디오 파일 처리기
 * M4A는 MPEG-4 컨테이너 포맷으로, MP3와 달리 단순 바이트 자르기가 어려움
 * 간단한 구현을 위해 비트레이트 기반 추정치로 처리
 */
@Component
@Slf4j
public class M4aAudioProcessor implements AudioProcessor {

    private static final int MAX_FILE_SIZE_MB = 25;
    private static final int MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

    // M4A 기본 비트레이트 (AAC는 보통 128-256kbps)
    private static final int DEFAULT_BITRATE_KBPS = 128;

    private final int shortsDurationSeconds;

    public M4aAudioProcessor(
            @Value("${ai.shorts.duration.seconds:60}") int shortsDurationSeconds) {
        this.shortsDurationSeconds = shortsDurationSeconds;
    }

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

        try {
            AudioMetadata metadata = extractMetadata(originalAudio);

            // 원본이 목표 시간보다 짧으면 전체 반환
            if (metadata.getDurationSeconds() <= durationSeconds) {
                log.info("원본 오디오({:.1f}초)가 목표 시간({}초)보다 짧아 전체 반환",
                        metadata.getDurationSeconds(), durationSeconds);
                return originalAudio;
            }

            // M4A는 컨테이너 포맷이라 단순 자르기가 어려움
            // 비트레이트 기반으로 대략적인 바이트 수 계산
            int bytesPerSecond = (metadata.getBitrate() * 1000) / 8;
            int targetBytes = bytesPerSecond * durationSeconds;

            // M4A 헤더 보존을 위해 최소 헤더 크기 확보
            int headerSize = findMdatOffset(originalAudio);
            if (headerSize > 0 && targetBytes < headerSize + 1024) {
                targetBytes = headerSize + 1024;
            }

            log.info("M4A 쇼츠 생성: 비트레이트={}kbps, 목표={}초, 예상크기={}KB",
                    metadata.getBitrate(), durationSeconds, targetBytes / 1024);

            // 앞부분만 자르기 (M4A 컨테이너 구조상 완벽하지 않을 수 있음)
            return Arrays.copyOf(originalAudio, Math.min(targetBytes, originalAudio.length));

        } catch (AudioProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("M4A 쇼츠 생성 실패", e);
            throw new AudioProcessingException("M4A 쇼츠 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기본 쇼츠 길이로 쇼츠 생성
     */
    public byte[] createShortClip(byte[] originalAudio) {
        return createShortClip(originalAudio, shortsDurationSeconds);
    }

    @Override
    public AudioMetadata extractMetadata(byte[] audioData) {
        try {
            // M4A 파일에서 메타데이터 추출 시도
            int bitrate = DEFAULT_BITRATE_KBPS;
            double durationSeconds = 0;

            // mvhd box에서 duration 추출 시도
            int mvhdOffset = findBoxOffset(audioData, "mvhd");
            if (mvhdOffset > 0 && mvhdOffset + 24 < audioData.length) {
                // mvhd box 구조: version(1) + flags(3) + creation_time + modification_time + timescale(4) + duration(4 or 8)
                int version = audioData[mvhdOffset + 8] & 0xFF;
                int timescaleOffset = mvhdOffset + 8 + (version == 1 ? 20 : 12);
                int durationOffset = timescaleOffset + 4;

                if (durationOffset + 4 < audioData.length) {
                    ByteBuffer buffer = ByteBuffer.wrap(audioData);
                    buffer.order(ByteOrder.BIG_ENDIAN);

                    int timescale = buffer.getInt(timescaleOffset);
                    long duration;
                    if (version == 1) {
                        duration = buffer.getLong(durationOffset);
                    } else {
                        duration = buffer.getInt(durationOffset) & 0xFFFFFFFFL;
                    }

                    if (timescale > 0) {
                        durationSeconds = (double) duration / timescale;
                    }
                }
            }

            // duration을 못 가져오면 파일 크기로 추정
            if (durationSeconds <= 0) {
                durationSeconds = (audioData.length * 8.0) / (bitrate * 1000);
                log.warn("M4A 재생시간 정보 없음, 추정값 사용: {:.1f}초", durationSeconds);
            }

            // 비트레이트는 파일 크기와 재생시간으로 추정
            if (durationSeconds > 0) {
                bitrate = (int) ((audioData.length * 8.0) / (durationSeconds * 1000));
            }

            log.debug("M4A 메타데이터: 비트레이트={}kbps, 재생시간={:.1f}초", bitrate, durationSeconds);

            return new AudioMetadata(bitrate, durationSeconds);

        } catch (Exception e) {
            log.warn("M4A 메타데이터 추출 실패, 기본값 사용", e);
            // 기본값 반환 (파일 크기 기반 추정)
            double estimatedDuration = (audioData.length * 8.0) / (DEFAULT_BITRATE_KBPS * 1000);
            return new AudioMetadata(DEFAULT_BITRATE_KBPS, estimatedDuration);
        }
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

    /**
     * M4A 파일에서 특정 box의 오프셋 찾기
     */
    private int findBoxOffset(byte[] data, String boxName) {
        if (data == null || data.length < 8) {
            return -1;
        }

        int offset = 0;
        while (offset + 8 < data.length) {
            ByteBuffer buffer = ByteBuffer.wrap(data, offset, 4);
            buffer.order(ByteOrder.BIG_ENDIAN);
            int boxSize = buffer.getInt();

            if (boxSize < 8) {
                break;
            }

            String name = new String(data, offset + 4, 4);
            if (name.equals(boxName)) {
                return offset;
            }

            // moov 안에 있는 box 탐색
            if (name.equals("moov") || name.equals("trak") || name.equals("mdia") || name.equals("minf") || name.equals("stbl")) {
                int innerOffset = findBoxOffsetInner(data, offset + 8, offset + boxSize, boxName);
                if (innerOffset > 0) {
                    return innerOffset;
                }
            }

            offset += boxSize;
        }

        return -1;
    }

    private int findBoxOffsetInner(byte[] data, int start, int end, String boxName) {
        int offset = start;
        while (offset + 8 < end && offset + 8 < data.length) {
            ByteBuffer buffer = ByteBuffer.wrap(data, offset, 4);
            buffer.order(ByteOrder.BIG_ENDIAN);
            int boxSize = buffer.getInt();

            if (boxSize < 8) {
                break;
            }

            String name = new String(data, offset + 4, 4);
            if (name.equals(boxName)) {
                return offset;
            }

            // 중첩된 컨테이너 box 탐색
            if (name.equals("trak") || name.equals("mdia") || name.equals("minf") || name.equals("stbl")) {
                int innerOffset = findBoxOffsetInner(data, offset + 8, offset + boxSize, boxName);
                if (innerOffset > 0) {
                    return innerOffset;
                }
            }

            offset += boxSize;
        }
        return -1;
    }

    /**
     * mdat box 오프셋 찾기 (실제 오디오 데이터 시작점)
     */
    private int findMdatOffset(byte[] data) {
        return findBoxOffset(data, "mdat");
    }
}
