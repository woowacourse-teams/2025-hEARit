package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.KeywordListCondition;
import com.onair.hearit.dto.response.KeywordResponse;
import com.onair.hearit.infrastructure.KeywordRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public List<KeywordResponse> getKeywords(KeywordListCondition condition) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        Page<Keyword> keywords = keywordRepository.findAll(pageable);
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

    public List<KeywordResponse> getRecommendedKeyword(int size) {
        long seed = LocalDate.now().toEpochDay();
        List<Long> allIds = keywordRepository.findAllIds();
        List<Long> selectedIds = pickRandomIds(allIds, seed, size);
        List<Keyword> keywords = keywordRepository.findAllByIdIn(selectedIds);
        return keywords.stream()
                .map(KeywordResponse::from)
                .toList();
    }

    private List<Long> pickRandomIds(List<Long> allIds, long seed, int size) {
        int fetchSize = Math.min(size, allIds.size());
        Collections.shuffle(allIds, new Random(seed));
        return allIds.subList(0, fetchSize);
    }
} 