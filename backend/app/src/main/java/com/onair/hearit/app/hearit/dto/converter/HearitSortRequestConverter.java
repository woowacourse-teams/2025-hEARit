package com.onair.hearit.app.hearit.dto.converter;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import com.onair.hearit.app.hearit.dto.HearitSortRequest;
import com.onair.hearit.app.hearit.dto.param.HearitSortField;
import com.onair.hearit.app.hearit.dto.param.SortDirection;
import org.springframework.core.convert.converter.Converter;

public class HearitSortRequestConverter implements Converter<String, HearitSortRequest> {

    @Override
    public HearitSortRequest convert(String source) {
        if (source == null || source.isBlank()) {
            throw new InvalidInputException("정렬 조건이 필요합니다.");
        }

        String[] parts = source.split(",");
        String fieldPart = parts[0].trim();
        String directionPart = parts.length > 1 ? parts[1].trim() : "desc";

        HearitSortField field = HearitSortField.from(fieldPart);
        SortDirection direction = SortDirection.from(directionPart);

        return new HearitSortRequest(field, direction);

    }
}
