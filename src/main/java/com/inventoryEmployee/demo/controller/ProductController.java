package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.request.ProductRequest;
import com.inventoryEmployee.demo.dto.response.ProductResponse;
import com.inventoryEmployee.demo.entity.Category;
import com.inventoryEmployee.demo.entity.Inventory;
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
import java.util.stream.Collectors;

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
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product productEntity = mapToEntity(request);

        Product created = productService.createProduct(productEntity);
        return new ResponseEntity<>(mapToResponse(created), HttpStatus.CREATED);
    }

    // Global Search (Name, Category, Supplier, SKU)
    @GetMapping("/search-global")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<ProductResponse>> searchGlobal(
            @RequestParam String query,
            Pageable pageable) {

        Page<Product> products = productService.searchProductsGlobal(query, pageable);
        return ResponseEntity.ok(products.map(this::mapToResponse));
    }

    // Get product by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(mapToResponse(product));
    }

    // Get product by SKU
    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        Product product = productService.getProductBySku(sku);
        return ResponseEntity.ok(mapToResponse(product));
    }

    // Get product by barcode
    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ProductResponse> getProductByBarcode(@PathVariable String barcode) {
        Product product = productService.getProductByBarcode(barcode);
        return ResponseEntity.ok(mapToResponse(product));
    }

    // Get all products with pagination
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);

        Page<ProductResponse> responsePage = products.map(this::mapToResponse);

        return ResponseEntity.ok(responsePage);
    }

    // Update product
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                 @Valid @RequestBody ProductRequest request) {
        Product productEntity = mapToEntity(request);

        Product updated = productService.updateProduct(id, productEntity);
        return ResponseEntity.ok(mapToResponse(updated));
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
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<Product> products = productService.searchProducts(searchTerm, pageable);
        return ResponseEntity.ok(products.map(this::mapToResponse));
    }

    // Get products by category
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);

        Page<ProductResponse> responsePage = products.map(this::mapToResponse);

        return ResponseEntity.ok(responsePage);
    }

    // Get products by supplier
    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<ProductResponse>> getProductsBySupplier(
            @PathVariable Long supplierId,
            Pageable pageable) {
        Page<Product> products = productService.getProductsBySupplier(supplierId, pageable);

        Page<ProductResponse> responsePage = products.map(this::mapToResponse);

        return ResponseEntity.ok(responsePage);
    }

    // Get products expiring soon
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProductResponse>> getProductsExpiringSoon(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<Product> products = productService.getProductsExpiringSoon(daysAhead);

        List<ProductResponse> responseList = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // Get expired products
    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProductResponse>> getExpiredProducts() {
        List<Product> products = productService.getExpiredProducts();

        List<ProductResponse> responseList = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // Advanced search with filters
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<ProductResponse>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<Product> products = productService.searchProductsWithFilters(
                categoryId, supplierId, status, minPrice, maxPrice, searchTerm, pageable);

        Page<ProductResponse> responsePage = products.map(this::mapToResponse);

        return ResponseEntity.ok(responsePage);
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

    private ProductResponse mapToResponse(Product product){
        Inventory inventory = product.getInventory();
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .unit(product.getUnit())
                .imageUrl(product.getImageUrl())
                .manufacturingDate(product.getManufacturingDate())
                .expiryDate(product.getExpiryDate())
                .manufacturer(product.getManufacturer())
                .model(product.getModel())
                .status(product.getStatus())

                .categoryName(product.getCategory() != null ? product.getCategory().getName():null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId():null)

                .supplierName(product.getSupplier() != null ? product.getSupplier().getName():null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId():null)

                .quantityAvailable(inventory != null ? inventory.getQuantityAvailable():0)

                .isOutOfStock(inventory == null || inventory.getQuantityAvailable() <= 0)

                .isLowStock(inventory != null && inventory.getQuantityAvailable() <= inventory.getMinStockLevel())

                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

    }
}
