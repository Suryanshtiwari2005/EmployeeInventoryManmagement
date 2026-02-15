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

import java.util.List;

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

        // Check if category name already exists
        if (categoryRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = mapToEntity(request);

        Category created = categoryRepository.save(category);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Get category by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .filter(cat->!cat.getDeleted())
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

    //

    // Get root categories (no parent)
    @GetMapping("/root")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<Category>> getRootCategories() {
        List<Category> categories = categoryRepository.findByParentCategoryIsNull();
        return ResponseEntity.ok(categories);
    }

    // Get subcategories
    @GetMapping("/{id}/subcategories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable Long id) {
        List<Category> subcategories = categoryRepository.findByParentCategoryId(id);
        return ResponseEntity.ok(subcategories);
    }

     //

    // Update category
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .filter(cat->!cat.getDeleted())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Check if new name conflicts with another category
        if (!existing.getName().equals(request.getName()) &&
                categoryRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setCode(request.getCode());
        existing.setImageUrl(request.getImageUrl());

        //
        // Update parent category if provided
        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            existing.setParentCategory(parent);
        } else {
            existing.setParentCategory(null);
        }
        //

        Category updated = categoryRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    // Delete category (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .filter(cat->!cat.getDeleted())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setDeleted(true);
        categoryRepository.save(category);
        return ResponseEntity.noContent().build();
    }
    //
    // Search categories
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Category>> searchCategories(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<Category> categories = categoryRepository.searchByName(searchTerm, pageable);
        return ResponseEntity.ok(categories);
    }

    // Get categories with product count
    @GetMapping("/with-product-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Object[]>> getCategoriesWithProductCount() {
        List<Object[]> result = categoryRepository.findCategoriesWithProductCount();
        return ResponseEntity.ok(result);
    }
     //

    private Category mapToEntity(CategoryRequest request){
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .code(request.getCode())
                .imageUrl(request.getImageUrl())
                .build();

        if(request.getParentCategoryId() != null){
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(()->new RuntimeException("Parent Category Not Found: "+ request.getParentCategoryId()));

            category.setParentCategory(parent);
        }

        return category;
    }
}
