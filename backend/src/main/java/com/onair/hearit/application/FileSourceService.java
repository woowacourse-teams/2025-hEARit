package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.OriginalAudioResponse;
import com.onair.hearit.dto.response.ScriptResponse;
import com.onair.hearit.dto.response.ShortAudioResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileSourceService {

    private final String sourceBaseUrl;
    private final HearitRepository hearitRepository;

    public FileSourceService(@Value("${amazon.s3.bucket}") String sourceBaseUrl, HearitRepository hearitRepository) {
        this.sourceBaseUrl = sourceBaseUrl;
        this.hearitRepository = hearitRepository;
    }

    public OriginalAudioResponse getOriginalAudio(Long hearitId) {
        Hearit hearit = getHearitById(hearitId);
        return new OriginalAudioResponse(hearit.getId(), createFullUrl(hearit.getOriginalAudioUrl()));
    }

    public ShortAudioResponse getShortAudio(Long id) {
        Hearit hearit = getHearitById(id);
        return new ShortAudioResponse(hearit.getId(), createFullUrl(hearit.getShortAudioUrl()));
    }

    public ScriptResponse getScript(Long id) {
        Hearit hearit = getHearitById(id);
        return new ScriptResponse(hearit.getId(), createFullUrl(hearit.getScriptUrl()));
    }

    private Hearit getHearitById(Long id) {
        return hearitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("hearitId", String.valueOf(id)));
    }

    private String createFullUrl(String path) {
        return sourceBaseUrl + path;
    }
}
