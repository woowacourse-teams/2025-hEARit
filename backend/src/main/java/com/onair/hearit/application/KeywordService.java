package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.KeywordResponse;
import com.onair.hearit.infrastructure.KeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public List<KeywordResponse> getKeywords(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Keyword> keywords = keywordRepository.findAll(pageable);
        return keywords.stream()
                .map(KeywordResponse::from)
                .toList();
    }

    public KeywordResponse getKeyword(Long id) {
        Keyword keyword = getKeywordById(id);
        return KeywordResponse.from(keyword);
    }

    private Keyword getKeywordById(Long id) {
        return keywordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("keywordId", String.valueOf(id)));
    }
}
