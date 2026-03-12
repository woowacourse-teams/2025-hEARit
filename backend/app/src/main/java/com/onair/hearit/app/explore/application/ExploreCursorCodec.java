package com.onair.hearit.app.explore.application;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import com.onair.hearit.app.explore.dto.ExploreCursor;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ExploreCursorCodec {

    private static final String DELIMITER = ":";

    public static String encode(ExploreCursor cursor) {
        String raw = cursor.score() + DELIMITER + cursor.hearitId();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static ExploreCursor decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return ExploreCursor.initial();
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            String raw = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = raw.split(DELIMITER, 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid cursor format: " + raw);
            }
            return new ExploreCursor(
                    Double.parseDouble(parts[0]),
                    Long.parseLong(parts[1])
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("잘못된 커서 형식입니다: " + encoded);
        }
    }
}
