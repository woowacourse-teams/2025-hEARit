package com.onair.hearit.admin.ai.infrastructure.audio;

/**
 * 오디오 자르기 및 메타데이터 추출 인터페이스
 * 구현체는 FFmpeg, 외부 서비스 등 다양한 방식으로 오디오를 처리할 수 있음
 */
public interface AudioClipper {

    /**
     * 오디오 파일에서 앞부분 N초를 정확하게 잘라냄
     *
     * @param audioData 원본 오디오 바이트 배열
     * @param extension 파일 확장자 (mp3, m4a 등)
     * @param durationSeconds 자를 길이 (초)
     * @return 잘린 오디오 바이트 배열
     */
    byte[] clip(byte[] audioData, String extension, int durationSeconds);

    /**
     * 오디오 파일의 재생 시간 추출
     *
     * @param audioData 오디오 바이트 배열
     * @param extension 파일 확장자
     * @return 재생 시간 (초), 추출 실패 시 -1
     */
    double getDuration(byte[] audioData, String extension);

    /**
     * 오디오 파일의 비트레이트 추출
     *
     * @param audioData 오디오 바이트 배열
     * @param extension 파일 확장자
     * @return 비트레이트 (kbps), 추출 실패 시 -1
     */
    int getBitrate(byte[] audioData, String extension);
}
