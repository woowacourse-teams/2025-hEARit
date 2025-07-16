package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;

public record OriginalAudioResponse(Long id, String url) {

    public static OriginalAudioResponse from(Hearit hearit) {
        return new OriginalAudioResponse(hearit.getId(), hearit.getOriginalAudioUrl());
    }
}
