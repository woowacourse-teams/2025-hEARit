package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.KeywordCreateRequest;
import com.onair.hearit.admin.dto.response.KeywordInfoResponse;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.infrastructure.KeywordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminKeywordService {

    private final KeywordRepository keywordRepository;

    public PagedResponse<KeywordInfoResponse> getKeywords(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Keyword> pageKeywords = keywordRepository.findAll(pageable);
        Page<KeywordInfoResponse> dtoPage = pageKeywords.map(KeywordInfoResponse::from);
        return PagedResponse.from(dtoPage);
    }

    @Transactional
    public KeywordInfoResponse addKeyword(KeywordCreateRequest request) {
        Keyword keyword = new Keyword(request.name());
        Keyword saved = keywordRepository.save(keyword);
        return KeywordInfoResponse.from(saved);
    }
}
