package com.onair.hearit.app.common.config;

import com.onair.hearit.app.hearit.dto.converter.HearitSortRequestConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new HearitSortRequestConverter());
    }
}
