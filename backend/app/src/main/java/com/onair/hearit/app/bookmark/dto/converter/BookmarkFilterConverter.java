package com.onair.hearit.app.bookmark.dto.converter;

import com.onair.hearit.app.bookmark.dto.param.BookmarkFilter;
import org.springframework.core.convert.converter.Converter;

public class BookmarkFilterConverter implements Converter<String, BookmarkFilter> {

    @Override
    public BookmarkFilter convert(String source) {
        return BookmarkFilter.fromName(source);
    }
}
