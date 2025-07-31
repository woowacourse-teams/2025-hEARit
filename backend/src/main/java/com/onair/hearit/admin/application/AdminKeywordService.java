package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.KeywordCreateRequest;
import com.onair.hearit.admin.dto.request.KeywordUpdateRequest;
import com.onair.hearit.admin.dto.response.KeywordInfoResponse;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.infrastructure.KeywordRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminKeywordService {

    private final KeywordRepository keywordRepository;

    public PagedResponse<KeywordInfoResponse> getKeywords(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(), Sort.by(Sort.Order.asc("id")));
        Page<Keyword> pageKeywords = keywordRepository.findAll(pageable);
        Page<KeywordInfoResponse> dtoPage = pageKeywords.map(KeywordInfoResponse::from);
        return PagedResponse.from(dtoPage);
    }

    public List<KeywordInfoResponse> getAllKeywords() {
        List<Keyword> allKeywords = keywordRepository.findAll();
        return allKeywords.stream()
                .map(KeywordInfoResponse::from)
                .toList();
    }

    public void addKeyword(KeywordCreateRequest request) {
        Keyword keyword = new Keyword(request.name());
        keywordRepository.save(keyword);
    }


    @Transactional
    public void updateKeyword(Long keywordId, KeywordUpdateRequest request) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new NotFoundException("keywordId", keywordId.toString()));
        keyword.updateName(request.name());
    }
}
