package com.onair.hearit.app.explore.dto;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record ExploreCursor(
        double score,
        long hearitId
) {
    private static final String DELIMITER = ":";

    public static ExploreCursor initial() {
        return new ExploreCursor(Double.MAX_VALUE, Long.MAX_VALUE);
    }

    public static ExploreCursor from(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return initial();
        }
        try {
            return decode(cursor);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("잘못된 커서 형식입니다: " + cursor);
        }
    }

    public boolean isInitial() {
        return score == Double.MAX_VALUE;
    }

    public String encode() {
        String raw = score + DELIMITER + hearitId;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static ExploreCursor decode(String encoded) {
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
    }
}
