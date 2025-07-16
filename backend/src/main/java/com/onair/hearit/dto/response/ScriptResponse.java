package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;

public record ScriptResponse(Long id, String url) {

    public static ScriptResponse from(Hearit hearit) {
        return new ScriptResponse(hearit.getId(), hearit.getScriptUrl());
    }
}
