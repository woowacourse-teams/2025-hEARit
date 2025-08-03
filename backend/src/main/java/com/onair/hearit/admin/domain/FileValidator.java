package com.onair.hearit.admin.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    public static void validateAll(MultipartFile original, MultipartFile shortAudio, MultipartFile script) {
        FileType.ORIGINAL.validateFilename(original);
        FileType.SHORT.validateFilename(shortAudio);
        FileType.SCRIPT.validateFilename(script);
        validateSameUuid(original, shortAudio, script);
    }

    private static void validateSameUuid(MultipartFile... files) {
        String base = extractUuid(files[0].getOriginalFilename());
        for (MultipartFile file : files) {
            String uuid = extractUuid(file.getOriginalFilename());
            if (!base.equals(uuid)) {
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
}
