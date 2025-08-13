package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.AdminPagingRequest;
import com.onair.hearit.admin.dto.request.CategoryCreateRequest;
import com.onair.hearit.admin.dto.request.CategoryUpdateRequest;
import com.onair.hearit.admin.dto.response.AdminCategoryResponse;
import com.onair.hearit.admin.dto.response.AdminPagedResponse;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.infrastructure.CategoryRepository;
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
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    public AdminPagedResponse<AdminCategoryResponse> getCategories(AdminPagingRequest pagingRequest) {
        Sort sort = Sort.by(Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(), sort);
        Page<Category> pageKeywords = categoryRepository.findAll(pageable);
        Page<AdminCategoryResponse> dtoPage = pageKeywords.map(AdminCategoryResponse::from);
        return AdminPagedResponse.from(dtoPage);
    }

    public List<AdminCategoryResponse> getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        return allCategories.stream()
                .map(AdminCategoryResponse::from)
                .toList();
    }

    @Transactional
    public void addCategory(CategoryCreateRequest request) {
        Category category = new Category(request.name(), request.colorCode());
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = getCategoryById(categoryId);
        category.update(request.name(), request.colorCode());
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("categoryId", id.toString()));
    }
}
