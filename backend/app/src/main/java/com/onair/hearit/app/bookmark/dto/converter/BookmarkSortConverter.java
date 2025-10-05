package com.onair.hearit.app.bookmark.dto.converter;

import com.onair.hearit.app.bookmark.dto.param.BookmarkSort;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.convert.converter.Converter;

public class BookmarkSortConverter implements Converter<String, BookmarkSort> {

    @Override
    public BookmarkSort convert(String source) {
        List<String> sources = Arrays.stream(source.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (sources.size() == 1) {
            return BookmarkSort.from(sources.get(0));
        }
        if (sources.size() == 2) {
            return BookmarkSort.of(sources.get(0), sources.get(1));
        }
        throw new IllegalArgumentException();
    }
}
