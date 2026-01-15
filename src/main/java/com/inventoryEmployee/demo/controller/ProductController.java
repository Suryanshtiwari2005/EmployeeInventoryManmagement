package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.ProductRequest;
import com.inventoryEmployee.demo.entity.Category;
import com.inventoryEmployee.demo.entity.Product;
import com.inventoryEmployee.demo.entity.Supplier;
import com.inventoryEmployee.demo.enums.ProductStatus;
import com.inventoryEmployee.demo.repository.CategoryRepository;
import com.inventoryEmployee.demo.repository.SupplierRepository;
import com.inventoryEmployee.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    // Create product
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request) {
        Product productEntity = mapToEntity(request);

        Product created = productService.createProduct(productEntity);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Get product by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // Get product by SKU
    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        Product product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    // Get product by barcode
    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Product> getProductByBarcode(@PathVariable String barcode) {
        Product product = productService.getProductByBarcode(barcode);
        return ResponseEntity.ok(product);
    }

    // Get all products with pagination
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    // Update product
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @Valid @RequestBody ProductRequest request) {
        Product productEntity = mapToEntity(request);

        Product updated = productService.updateProduct(id, productEntity);
        return ResponseEntity.ok(updated);
    }

    // Delete product (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Search products
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<Product> products = productService.searchProducts(searchTerm, pageable);
        return ResponseEntity.ok(products);
    }

    // Get products by category
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    // Get products by supplier
    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<Product>> getProductsBySupplier(
            @PathVariable Long supplierId,
            Pageable pageable) {
        Page<Product> products = productService.getProductsBySupplier(supplierId, pageable);
        return ResponseEntity.ok(products);
    }

    // Get products expiring soon
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Product>> getProductsExpiringSoon(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<Product> products = productService.getProductsExpiringSoon(daysAhead);
        return ResponseEntity.ok(products);
    }

    // Get expired products
    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Product>> getExpiredProducts() {
        List<Product> products = productService.getExpiredProducts();
        return ResponseEntity.ok(products);
    }

    // Advanced search with filters
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Product>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<Product> products = productService.searchProductsWithFilters(
                categoryId, supplierId, status, minPrice, maxPrice, searchTerm, pageable);
        return ResponseEntity.ok(products);
    }

    private Product mapToEntity(ProductRequest request){
        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setUnit(request.getUnit());
        product.setImageUrl(request.getImageUrl());
        product.setManufacturingDate(request.getManufacturingDate());
        product.setExpiryDate(request.getExpiryDate());
        product.setManufacturer(request.getManufacturer());
        product.setModel(request.getModel());
        product.setStatus(request.getStatus());

        // Map Category (Relationship)
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Map Supplier (Relationship)
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + request.getSupplierId()));
            product.setSupplier(supplier);
        }

        return product;
    }
}
