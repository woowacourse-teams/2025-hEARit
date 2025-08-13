package com.onair.hearit.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUrls {

    private String originalAudioUrl;
    private String shortAudioUrl;
    private String scriptUrl;

    public FileUrls(final String originalAudioUrl, final String shortAudioUrl, final String scriptUrl) {
        this.originalAudioUrl = originalAudioUrl;
        this.shortAudioUrl = shortAudioUrl;
        this.scriptUrl = scriptUrl;
    }

    // Wither 메서드 추가
    public FileUrls withOriginalAudioUrl(String newOriginalAudioUrl) {
        return new FileUrls(newOriginalAudioUrl, this.shortAudioUrl, this.scriptUrl);
    }

    public FileUrls withShortAudioUrl(String newShortAudioUrl) {
        return new FileUrls(this.originalAudioUrl, newShortAudioUrl, this.scriptUrl);
    }

    public FileUrls withScriptUrl(String newScriptUrl) {
        return new FileUrls(this.originalAudioUrl, this.shortAudioUrl, newScriptUrl);
    }
}
