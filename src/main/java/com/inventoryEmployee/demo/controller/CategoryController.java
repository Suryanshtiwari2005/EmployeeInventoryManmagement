package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.request.CategoryRequest;
import com.inventoryEmployee.demo.entity.Category;
import com.inventoryEmployee.demo.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // Create category
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = maptoEntity(request);

        Category created = categoryRepository.save(category);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Get category by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return ResponseEntity.ok(category);
    }

    // Get all categories
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Category>> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findByDeletedFalse(pageable);
        return ResponseEntity.ok(categories);
    }

    // Update category
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @Valid @RequestBody Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setCode(category.getCode());
        existing.setImageUrl(category.getImageUrl());

        Category updated = categoryRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    // Delete category (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setDeleted(true);
        categoryRepository.save(category);
        return ResponseEntity.noContent().build();
    }

    private Category maptoEntity(CategoryRequest request){
        Category category = new Category();

        request.setName(request.getName());
        request.setDescription(request.getDescription());
        request.setCode(request.getCode());
        request.setImageUrl(request.getImageUrl());
        request.setParentCategoryId(request.getParentCategoryId());

        if(request.getParentCategoryId() != null){
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(()->new RuntimeException("Parent Category Not Found: "+ request.getParentCategoryId()));

            category.setParentCategory(parent);
        }

        return category;
    }
}
