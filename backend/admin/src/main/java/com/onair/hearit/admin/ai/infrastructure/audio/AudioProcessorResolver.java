package com.onair.hearit.admin.ai.infrastructure.audio;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AudioProcessorResolver {

    private final List<AudioProcessor> processors;

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

    public String getSupportedFormats() {
        return processors.stream()
                .map(AudioProcessor::getExtension)
                .collect(Collectors.joining(", "));
    }

    public boolean isSupported(String filename) {
        return processors.stream().anyMatch(p -> p.supports(filename));
    }
}
