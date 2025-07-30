package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.CategoryCreateRequest;
import com.onair.hearit.admin.dto.response.CategoryInfoResponse;
import com.onair.hearit.domain.Category;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    public PagedResponse<CategoryInfoResponse> getCategories(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(), Sort.by(Sort.Order.asc("id")));
        Page<Category> pageKeywords = categoryRepository.findAll(pageable);
        Page<CategoryInfoResponse> dtoPage = pageKeywords.map(CategoryInfoResponse::from);
        return PagedResponse.from(dtoPage);
    }

    public List<CategoryInfoResponse> getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        return allCategories.stream()
                .map(CategoryInfoResponse::from)
                .toList();
    }

    public void addCategory(CategoryCreateRequest request) {
        Category category = new Category(request.name(), request.colorCode());
        categoryRepository.save(category);
    }
}
