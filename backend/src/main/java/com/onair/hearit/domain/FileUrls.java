package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Embeddable;
import java.util.List;
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
        validateAll(originalAudioUrl, shortAudioUrl, scriptUrl);
        this.originalAudioUrl = originalAudioUrl;
        this.shortAudioUrl = shortAudioUrl;
        this.scriptUrl = scriptUrl;
    }

    private void validateAll(String originalAudioPath, String shortAudioPath, String scriptFilePath) {
        FileType.ORIGINAL.validUrlName(originalAudioPath);
        FileType.SHORT.validUrlName(shortAudioPath);
        FileType.SCRIPT.validUrlName(scriptFilePath);
        validateSameUuid(List.of(originalAudioPath, shortAudioPath, scriptFilePath));
    }

    private void validateSameUuid(List<String> fileNames) {
        String uuid = extractUuid(fileNames.getFirst());
        for (String fileName : fileNames) {
            if (!uuid.equals(extractUuid(fileName))) {
                throw new InvalidInputException("파일들이 동일한 리소스를 나타내지 않습니다.");
            }
        }
    }

    private String extractUuid(String filename) {
        int underscoreIndex = filename.indexOf('_');
        int dotIndex = filename.lastIndexOf('.');
        if (underscoreIndex == -1 || dotIndex == -1 || underscoreIndex >= dotIndex) {
            throw new InvalidInputException("파일명 형식이 올바르지 않습니다: " + filename);
        }
        return filename.substring(underscoreIndex + 1, dotIndex);
    }


    public FileUrls updateOriginalAudioUrl(String newOriginalAudioUrl) {
        return new FileUrls(newOriginalAudioUrl, this.shortAudioUrl, this.scriptUrl);
    }

    public FileUrls updateShortAudioUrl(String newShortAudioUrl) {
        return new FileUrls(this.originalAudioUrl, newShortAudioUrl, this.scriptUrl);
    }

    public FileUrls updateScriptUrl(String newScriptUrl) {
        return new FileUrls(this.originalAudioUrl, this.shortAudioUrl, newScriptUrl);
    }
}
