package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.HearitDomainException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {

    ORIGINAL("ORG", ".mp3", "/hearit/audio/original/") {
        @Override
        public FileUrls updateFileUrls(FileUrls urls, String newUrl) {
            return urls.updateOriginalAudioUrl(newUrl);
        }

        @Override
        public String getFileUrls(FileUrls urls) {
            return urls.getOriginalAudioUrl();
        }
    },
    SHORT("SHR", ".mp3", "/hearit/audio/short/") {
        @Override
        public FileUrls updateFileUrls(FileUrls urls, String newUrl) {
            return urls.updateShortAudioUrl(newUrl);
        }

        @Override
        public String getFileUrls(FileUrls urls) {
            return urls.getShortAudioUrl();
        }
    },
    SCRIPT("SCR", ".json", "/hearit/script/") {
        @Override
        public FileUrls updateFileUrls(FileUrls urls, String newUrl) {
            return urls.updateScriptUrl(newUrl);
        }

        @Override
        public String getFileUrls(FileUrls urls) {
            return urls.getScriptUrl();
        }
    };

    private final String prefix;
    private final String extension;
    private final String uploadPath;

    public abstract FileUrls updateFileUrls(FileUrls urls, String newUrl);

    public abstract String getFileUrls(FileUrls urls);

    public void validateFileName(String fileName) {
        if (!fileName.startsWith(prefix) || !fileName.endsWith(extension)) {
            throw new HearitDomainException("파일명은 '" + prefix + "로 시작하고, " + extension + "확장자여야합니다.");
        }
    }

    public void validateKey(String key) {
        if (!key.startsWith(uploadPath)) {
            throw new HearitDomainException(this.name() + "의 파일명은 '" + uploadPath + "'로 시작해야 합니다.");
        }
        if (!key.endsWith(extension)) {
            throw new HearitDomainException(this.name() + "의 파일 확장자는 '" + extension + "' 이어야 합니다.");
        }
    }

    public String generateKey(String fileName) {
        return uploadPath + fileName;
    }
}
