package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;

public record ShortAudioResponse(Long id, String url) {

    public static ShortAudioResponse from(Hearit hearit) {
        return new ShortAudioResponse(hearit.getId(), hearit.getShortAudioUrl());
    }
}
