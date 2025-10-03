package com.onair.hearit.app.hearit.config;

import com.onair.hearit.app.hearit.dto.converter.HearitSortConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RequestFormatConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new HearitSortConverter());
    }
}
