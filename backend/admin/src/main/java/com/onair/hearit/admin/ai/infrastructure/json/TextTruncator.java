package com.onair.hearit.admin.ai.infrastructure.json;

import java.text.BreakIterator;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class TextTruncator {

    private static final Locale KOREAN = Locale.KOREAN;

    public String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        BreakIterator iterator = BreakIterator.getCharacterInstance(KOREAN);
        iterator.setText(text);

        int end = 0;
        int boundary = iterator.first();
        while (boundary != BreakIterator.DONE && boundary <= maxLength) {
            end = boundary;
            boundary = iterator.next();
        }

        return text.substring(0, end);
    }

    public String truncateWithEllipsis(String text, int maxLength, String ellipsis) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        int ellipsisLength = ellipsis != null ? ellipsis.length() : 0;
        int targetLength = maxLength - ellipsisLength;

        if (targetLength <= 0) {
            return ellipsis != null ? ellipsis.substring(0, Math.min(ellipsis.length(), maxLength)) : "";
        }

        String truncated = truncate(text, targetLength);
        return truncated + (ellipsis != null ? ellipsis : "");
    }

    public String truncateWithEllipsis(String text, int maxLength) {
        return truncateWithEllipsis(text, maxLength, "...");
    }
}
