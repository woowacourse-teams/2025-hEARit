package com.onair.hearit.admin.ai.infrastructure.audio;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 오디오 처리 인터페이스
 * 다양한 오디오 포맷(MP3, M4A 등)을 지원하기 위한 추상화
 */
public interface AudioProcessor {

    /**
     * 해당 파일을 처리할 수 있는지 확인
     *
     * @param filename 파일명 (확장자 포함)
     * @return 지원 여부
     */
    boolean supports(String filename);

    /**
     * 오디오 파일 유효성 검증
     *
     * @param data 오디오 바이트 배열
     * @param filename 파일명
     * @throws com.onair.hearit.admin.ai.exception.AudioProcessingException 유효하지 않은 경우
     */
    void validate(byte[] data, String filename);

    /**
     * 오디오 파일에서 앞부분 N초를 잘라 쇼츠 생성
     *
     * @param originalAudio 원본 오디오 바이트 배열
     * @param durationSeconds 자를 길이 (초)
     * @return 잘린 오디오 바이트 배열
     */
    byte[] createShortClip(byte[] originalAudio, int durationSeconds);

    /**
     * 오디오 메타데이터 추출
     *
     * @param audioData 오디오 바이트 배열
     * @return 메타데이터 (비트레이트, 재생시간)
     */
    AudioMetadata extractMetadata(byte[] audioData);

    /**
     * 지원하는 파일 확장자 반환
     *
     * @return 확장자 (예: "mp3", "m4a")
     */
    String getExtension();

    /**
     * MIME 타입 반환
     *
     * @return MIME 타입 (예: "audio/mpeg", "audio/mp4")
     */
    String getMimeType();

    /**
     * 오디오 메타데이터 DTO
     */
    @Getter
    @AllArgsConstructor
    class AudioMetadata {
        private final int bitrate;            // kbps
        private final double durationSeconds; // 재생 시간 (초)

        public int getDurationSecondsInt() {
            return (int) Math.ceil(durationSeconds);
        }
    }
}
