package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {

    ORIGINAL("ORG", ".mp3", "hearit/audio/original/"),
    SHORT("SHR", ".mp3", "hearit/audio/short/"),
    SCRIPT("SCR", ".json", "hearit/script/");

    private final String prefix;
    private final String extension;
    private final String uploadPath;

    public void validateFilename(String filename) {
        if (!filename.startsWith(prefix)) {
            throw new InvalidInputException(this.name() + "의 파일명은 '" + prefix + "'로 시작해야 합니다.");
        }
        if (!filename.endsWith(extension)) {
            throw new InvalidInputException(this.name() + "의 파일 확장자는 '" + extension + "' 이어야 합니다.");
        }
    }
}
