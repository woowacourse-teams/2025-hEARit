package com.onair.hearit.dto.response;

public record HearitCreationResponse(
        String title,
        String script,
        String audioUrl) {

    public static HearitCreationResponse from(String title, String script, String audioUrl) {
        return new HearitCreationResponse(
                title,
                script,
                audioUrl
        );
    }
}
