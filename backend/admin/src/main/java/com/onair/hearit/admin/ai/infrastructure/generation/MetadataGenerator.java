package com.onair.hearit.admin.ai.infrastructure.generation;

import lombok.AllArgsConstructor;
import lombok.Getter;


public interface MetadataGenerator {


    GeneratedMetadata generate(String scriptText);

    @Getter
    @AllArgsConstructor
    class GeneratedMetadata {
        private final String title;
        private final String summary;
    }
}
