package com.onair.hearit.app.bookmark;

import org.springframework.core.convert.converter.Converter;

public class StringToBookmarkFilterConverter implements Converter<String, BookmarkFilter> {

    @Override
    public BookmarkFilter convert(String source) {
        return BookmarkFilter.fromName(source);
    }
}
