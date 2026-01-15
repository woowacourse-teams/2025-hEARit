package com.onair.hearit.admin.ai.application;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.openai.WhisperClient;
import com.onair.hearit.admin.ai.infrastructure.openai.WhisperClient.TranscriptionResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * STT(Speech-to-Text) 처리 서비스
 * OpenAI Whisper API를 사용하여 오디오를 텍스트로 변환
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TranscriptionService {

    private final WhisperClient whisperClient;

    /**
     * 오디오 파일을 텍스트로 변환
     *
     * @param audioData 오디오 바이트 배열
     * @param filename 파일명
     * @return STT 결과 (재생시간 + 세그먼트 목록)
     */
    public TranscriptionResult transcribe(byte[] audioData, String filename) {
        log.info("STT 처리 시작: 파일={}, 크기={}KB", filename, audioData.length / 1024);

        TranscriptionResult result = whisperClient.transcribe(audioData, filename);

        log.info("STT 처리 완료: 재생시간={}초, 세그먼트={}개",
                result.getDurationSeconds(), result.getSegments().size());

        return result;
    }

    /**
     * 세그먼트 목록을 하나의 텍스트로 병합
     */
    public String mergeSegmentsToText(List<ScriptSegment> segments) {
        StringBuilder sb = new StringBuilder();
        for (ScriptSegment segment : segments) {
            if (!segment.getText().isEmpty()) {
                sb.append(segment.getText()).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
