package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.AdminCategoryResponse;
import com.onair.hearit.admin.dto.request.AdminPagedResponse;
import com.onair.hearit.admin.dto.request.AdminPagingRequest;
import com.onair.hearit.admin.dto.request.CategoryCreateRequest;
import com.onair.hearit.admin.dto.request.CategoryUpdateRequest;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.infrastructure.jpa.CategoryRepository;
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
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    public AdminPagedResponse<AdminCategoryResponse> getCategories(AdminPagingRequest pagingRequest) {
        Sort sort = Sort.by(Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(), sort);
        Page<Category> pageCategories = categoryRepository.findAll(pageable);
        Page<AdminCategoryResponse> dtoPage = pageCategories.map(AdminCategoryResponse::from);
        return AdminPagedResponse.from(dtoPage);
    }

    public List<AdminCategoryResponse> getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        return allCategories.stream()
                .map(AdminCategoryResponse::from)
                .toList();
    }

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
