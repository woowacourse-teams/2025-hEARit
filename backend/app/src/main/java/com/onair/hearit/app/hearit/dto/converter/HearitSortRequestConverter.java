package com.onair.hearit.app.hearit.dto.converter;

import com.onair.hearit.app.hearit.dto.HearitSortRequest;
import com.onair.hearit.app.hearit.dto.param.HearitSortField;
import com.onair.hearit.app.hearit.dto.param.SortDirection;
import org.springframework.core.convert.converter.Converter;

public class HearitSortRequestConverter implements Converter<String, HearitSortRequest> {

    @Override
    public HearitSortRequest convert(String source) {
        String[] parts = source.split(",");
        String fieldPart = parts[0].trim();
        String directionPart = extractDirection(parts);

        HearitSortField field = HearitSortField.from(fieldPart);
        SortDirection direction = SortDirection.from(directionPart);

        return new HearitSortRequest(field, direction);

    }

    private String extractDirection(String[] parts) {
        if (parts.length > 1) {
            return parts[1].trim();
        }
        return "desc";
    }
}
