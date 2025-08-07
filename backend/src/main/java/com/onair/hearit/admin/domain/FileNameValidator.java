package com.onair.hearit.admin.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.domain.FileType;
import com.onair.hearit.domain.Hearit;
import java.util.List;

public class FileNameValidator {

    public static void validateAll(String originalAudioPath, String shortAudioPath, String scriptFilePath) {
        FileType.ORIGINAL.validateFilename(originalAudioPath);
        FileType.SHORT.validateFilename(shortAudioPath);
        FileType.SCRIPT.validateFilename(scriptFilePath);
        validateSameUuid(List.of(originalAudioPath, shortAudioPath, scriptFilePath));
    }

    private static void validateSameUuid(List<String> fileNames) {
        String uuid = extractUuid(fileNames.getFirst());
        for (String fileName : fileNames) {
            if (!uuid.equals(extractUuid(fileName))) {
                throw new InvalidInputException("파일들이 동일한 리소스를 나타내지 않습니다.");
            }
        }
    }

    private static String extractUuid(String filename) {
        int underscoreIndex = filename.indexOf('_');
        int dotIndex = filename.lastIndexOf('.');
        if (underscoreIndex == -1 || dotIndex == -1 || underscoreIndex >= dotIndex) {
            throw new InvalidInputException("파일명 형식이 올바르지 않습니다: " + filename);
        }
        return filename.substring(underscoreIndex + 1, dotIndex);
    }

    public static void validateFileUrl(String fileUrl, FileType fileType, Hearit hearit) {
        fileType.validateFilename(fileUrl);
        validateSameUuid(List.of(hearit.getFileUrl(fileType), fileUrl));
    }
}
