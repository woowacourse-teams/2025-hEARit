// FileType.java - 수정됨
package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {

    ORIGINAL("ORG", ".mp3", "hearit/audio/original/") {
        @Override
        public FileUrls update(FileUrls urls, String newUrl) {
            return urls.updateOriginalAudioUrl(newUrl);
        }

        @Override
        public String get(FileUrls urls) {
            return urls.getOriginalAudioUrl();
        }
    },
    SHORT("SHR", ".mp3", "hearit/audio/short/") {
        @Override
        public FileUrls update(FileUrls urls, String newUrl) {
            return urls.updateShortAudioUrl(newUrl);
        }

        @Override
        public String get(FileUrls urls) {
            return urls.getShortAudioUrl();
        }
    },
    SCRIPT("SCR", ".json", "hearit/script/") {
        @Override
        public FileUrls update(FileUrls urls, String newUrl) {
            return urls.updateScriptUrl(newUrl);
        }

        @Override
        public String get(FileUrls urls) {
            return urls.getScriptUrl();
        }
    };

    private final String prefix;
    private final String extension;
    private final String uploadPath;

    public abstract FileUrls update(FileUrls urls, String newUrl);

    public abstract String get(FileUrls urls);

    public void validateFilename(String filename) {
        if (!filename.startsWith(prefix)) {
            throw new InvalidInputException(this.name() + "의 파일명은 '" + prefix + "'로 시작해야 합니다.");
        }
        if (!filename.endsWith(extension)) {
            throw new InvalidInputException(this.name() + "의 파일 확장자는 '" + extension + "' 이어야 합니다.");
        }
    }
}
