package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.OriginalAudioResponse;
import com.onair.hearit.dto.response.ScriptResponse;
import com.onair.hearit.dto.response.ShortAudioResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private final HearitRepository hearitRepository;

    public OriginalAudioResponse getOriginalAudio(Long hearitId) {
        Hearit hearit = findHearit(hearitId);
        return OriginalAudioResponse.from(hearit);
    }

    public ShortAudioResponse getShortAudio(Long id) {
        Hearit hearit = findHearit(id);
        return ShortAudioResponse.from(hearit);
    }

    public ScriptResponse getScript(Long id) {
        Hearit hearit = findHearit(id);
        return ScriptResponse.from(hearit);
    }

    private Hearit findHearit(Long id) {
        return hearitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("hearitId", String.valueOf(id)));
    }
}
