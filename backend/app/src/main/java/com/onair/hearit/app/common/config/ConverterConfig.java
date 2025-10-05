package com.onair.hearit.app.common.config;

import com.onair.hearit.app.bookmark.dto.converter.BookmarkFilterConverter;
import com.onair.hearit.app.bookmark.dto.converter.BookmarkSortConverter;
import com.onair.hearit.app.hearit.dto.converter.HearitSortRequestConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new HearitSortRequestConverter());
        registry.addConverter(new BookmarkFilterConverter());
        registry.addConverter(new BookmarkSortConverter());
    }
}
