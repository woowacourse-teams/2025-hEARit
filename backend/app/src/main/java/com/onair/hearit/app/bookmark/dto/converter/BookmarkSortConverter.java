package com.onair.hearit.app.bookmark.dto.converter;

import com.onair.hearit.app.bookmark.dto.param.BookmarkSort;
import org.springframework.core.convert.converter.Converter;

public class BookmarkSortConverter implements Converter<String, BookmarkSort> {

    @Override
    public BookmarkSort convert(String source) {
        String[] sources = source.split(",");
        if (sources.length == 2) {
            return BookmarkSort.of(sources[0].trim(), sources[1].trim());
        }
        return BookmarkSort.from(sources[0].trim());
    }
}
