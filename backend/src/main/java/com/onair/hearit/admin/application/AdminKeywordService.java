package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.AdminPagingRequest;
import com.onair.hearit.admin.dto.request.KeywordCreateRequest;
import com.onair.hearit.admin.dto.request.KeywordUpdateRequest;
import com.onair.hearit.admin.dto.response.AdminKeywordResponse;
import com.onair.hearit.admin.dto.response.AdminPagedResponse;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.infrastructure.KeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminKeywordService {

    private final KeywordRepository keywordRepository;

    public AdminPagedResponse<AdminKeywordResponse> getKeywords(AdminPagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(), Sort.by(Sort.Order.asc("id")));
        Page<Keyword> pageKeywords = keywordRepository.findAll(pageable);
        Page<AdminKeywordResponse> dtoPage = pageKeywords.map(AdminKeywordResponse::from);
        return AdminPagedResponse.from(dtoPage);
    }

    public List<AdminKeywordResponse> getAllKeywords() {
        List<Keyword> allKeywords = keywordRepository.findAll();
        return allKeywords.stream()
                .map(AdminKeywordResponse::from)
                .toList();
    }

    public void addKeyword(KeywordCreateRequest request) {
        Keyword keyword = new Keyword(request.name());
        keywordRepository.save(keyword);
    }

    @Transactional
    public void updateKeyword(Long keywordId, KeywordUpdateRequest request) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new AdminNotFoundException("keywordId", keywordId.toString()));
        keyword.updateName(request.name());
    }
}
