package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.KeywordResponse;
import com.onair.hearit.infrastructure.KeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public List<KeywordResponse> getKeywords() {
        List<Keyword> keywords = keywordRepository.findAll();
        return keywords.stream()
                .map(KeywordResponse::from)
                .toList();
    }

    public KeywordResponse getKeyword(final Long id) {
        Keyword keyword = findKeyword(id);
        return KeywordResponse.from(keyword);
    }

    private Keyword findKeyword(final Long id) {
        return keywordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("keywordId", String.valueOf(id)));
    }

    public List<KeywordResponse> getRecommendedKeyword(long seed, int size) {
        List<Keyword> keywords = keywordRepository.findRandomWithSeed(seed, size);
        return keywords.stream()
                .map(KeywordResponse::from)
                .toList();
    }
} 