package com.onair.hearit.admin.ai.infrastructure.generation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 메타데이터를 생성하는 행위를 정의하는 인터페이스
 *
 * 책임:
 * - 대본 텍스트에서 제목과 요약 생성
 */
public interface MetadataGenerator {

    /**
     * 메타데이터 생성
     *
     * @param scriptText 전체 대본 텍스트
     * @return 생성된 메타데이터 (제목, 요약)
     */
    GeneratedMetadata generate(String scriptText);

    /**
     * 생성된 메타데이터 DTO
     */
    @Getter
    @AllArgsConstructor
    class GeneratedMetadata {
        private final String title;
        private final String summary;
    }
}
