package com.onair.hearit.admin.ai.infrastructure.audio;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 파일 확장자에 따라 적절한 AudioProcessor를 선택하는 Resolver
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AudioProcessorResolver {

    private final List<AudioProcessor> processors;

    /**
     * 파일명에 맞는 AudioProcessor 반환
     *
     * @param filename 파일명 (확장자 포함)
     * @return 해당 파일을 처리할 수 있는 AudioProcessor
     * @throws AudioProcessingException 지원하지 않는 포맷인 경우
     */
    public AudioProcessor resolve(String filename) {
        return processors.stream()
                .filter(p -> p.supports(filename))
                .findFirst()
                .orElseThrow(() -> {
                    String supportedFormats = getSupportedFormats();
                    log.warn("지원하지 않는 오디오 형식: {}, 지원 포맷: {}", filename, supportedFormats);
                    return AudioProcessingException.unsupportedFormat(
                            String.format("지원하지 않는 오디오 형식입니다. 지원 포맷: %s", supportedFormats));
                });
    }

    /**
     * 지원하는 모든 포맷 목록 반환
     */
    public String getSupportedFormats() {
        return processors.stream()
                .map(AudioProcessor::getExtension)
                .collect(Collectors.joining(", "));
    }

    /**
     * 해당 파일을 지원하는지 확인
     */
    public boolean isSupported(String filename) {
        return processors.stream().anyMatch(p -> p.supports(filename));
    }
}
