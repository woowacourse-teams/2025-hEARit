package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.OriginalAudioResponse;
import com.onair.hearit.dto.response.ScriptResponse;
import com.onair.hearit.dto.response.ShortAudioResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileSourceService {

    @Value("${amazon.s3.bucket}")
    private static String SOURCE_BASE_URL;

    private final HearitRepository hearitRepository;

    public OriginalAudioResponse getOriginalAudio(Long hearitId) {
        Hearit hearit = findHearit(hearitId);
        return new OriginalAudioResponse(hearit.getId(), createFullUrl(hearit.getOriginalAudioUrl()));
    }

    public ShortAudioResponse getShortAudio(Long id) {
        Hearit hearit = findHearit(id);
        return new ShortAudioResponse(hearit.getId(), createFullUrl(hearit.getShortAudioUrl()));
    }

    public ScriptResponse getScript(Long id) {
        Hearit hearit = findHearit(id);
        return new ScriptResponse(hearit.getId(), createFullUrl(hearit.getScriptUrl()));
    }

    private Hearit findHearit(Long id) {
        return hearitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("hearitId", String.valueOf(id)));
    }

    private String createFullUrl(String path) {
        return SOURCE_BASE_URL + path;
    }
}
