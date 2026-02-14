package com.onair.hearit.app.explore.dto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record ExploreCursor(
        double score,
        long hearitId
) {
    private static final String DELIMITER = ":";

    public String encode() {
        String raw = score + DELIMITER + hearitId;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static ExploreCursor decode(String encoded) {
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

    public static boolean isInitialRequest(String cursor) {
        return cursor == null || cursor.isBlank();
    }
}
