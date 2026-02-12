package com.onair.hearit.app.search.dto.convertor;

import com.onair.hearit.app.search.dto.SearchSortRequest;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitSearchSortField;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class SearchSortRequestConverter implements Converter<String, SearchSortRequest> {

    @Override
    public SearchSortRequest convert(String source) {
        HearitSearchSortField field = HearitSearchSortField.from(source.trim());
        return new SearchSortRequest(field);

    }
}
