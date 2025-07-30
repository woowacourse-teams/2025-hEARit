package com.onair.hearit.admin.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public enum FileType {
    ORIGINAL("ORG", ".mp3", "hearit/audio/original/"),
    SHORT("SHR", ".mp3", "hearit/audio/short/"),
    SCRIPT("SCR", ".json", "hearit/script/");

    private final String prefix;
    private final String extension;
    private final String uploadPath;

    FileType(String prefix, String extension, String uploadPath) {
        this.prefix = prefix;
        this.extension = extension;
        this.uploadPath = uploadPath;
    }

    public void validateFilename(MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        if (!filename.startsWith(prefix)) {
            throw new InvalidInputException(this.name() + "의 파일명은 '" + prefix + "'로 시작해야 합니다.");
        }
        if (!filename.endsWith(extension)) {
            throw new InvalidInputException(this.name() + "의 파일 확장자는 '" + extension + "' 이어야 합니다.");
        }
    }
}
