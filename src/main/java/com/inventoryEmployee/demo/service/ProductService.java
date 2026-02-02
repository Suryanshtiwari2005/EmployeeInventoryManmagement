package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Product;
import com.inventoryEmployee.demo.enums.ProductStatus;
import com.inventoryEmployee.demo.exception.ResourceNotFoundException;
import com.inventoryEmployee.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final InventoryService inventoryService;

    // Create new product
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getSku());

        // Check if SKU already exists
        if (productRepository.existsBySkuAndDeletedFalse(product.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists");
        }

        // Check if barcode already exists
        if (product.getBarcode() != null &&
                productRepository.existsByBarcodeAndDeletedFalse(product.getBarcode())) {
            throw new IllegalArgumentException("Product with barcode " + product.getBarcode() + " already exists");
        }

        Product savedProduct = productRepository.save(product);

        // Create inventory record for the product
        inventoryService.createInventoryForProduct(savedProduct);

        auditService.logAction("Product", savedProduct.getId(), "CREATE", null, savedProduct);

        return savedProduct;
    }

    // Get product by ID
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .filter(product -> !product.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    // Get product by SKU
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .filter(product -> !product.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
    }

    // Get product by barcode
    @Transactional(readOnly = true)
    public Product getProductByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .filter(product -> !product.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode: " + barcode));
    }

    // Get all products
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable);
    }

    // Global Search Service
    @Transactional(readOnly = true)
    public Page<Product> searchProductsGlobal(String keyword, Pageable pageable) {
        return productRepository.searchGlobal(keyword, pageable);
    }

    // Update product
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);

        log.info("Updating product: {}", id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setCostPrice(updatedProduct.getCostPrice());
        existingProduct.setUnit(updatedProduct.getUnit());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        existingProduct.setManufacturingDate(updatedProduct.getManufacturingDate());
        existingProduct.setExpiryDate(updatedProduct.getExpiryDate());
        existingProduct.setManufacturer(updatedProduct.getManufacturer());
        existingProduct.setModel(updatedProduct.getModel());
        existingProduct.setStatus(updatedProduct.getStatus());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setSupplier(updatedProduct.getSupplier());

        Product saved = productRepository.save(existingProduct);
        auditService.logAction("Product", id, "UPDATE", existingProduct, saved);

        return saved;
    }

    // Soft delete product
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        log.info("Soft deleting product: {}", id);

        product.setDeleted(true);
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);

        auditService.logAction("Product", id, "DELETE", product, null);
    }

    // Search products
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.searchByNameOrSku(searchTerm, pageable);
    }

    // Get products by category
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    // Get products by supplier
    @Transactional(readOnly = true)
    public Page<Product> getProductsBySupplier(Long supplierId, Pageable pageable) {
        return productRepository.findBySupplierId(supplierId, pageable);
    }

    // Get products expiring soon
    @Transactional(readOnly = true)
    public List<Product> getProductsExpiringSoon(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);
        return productRepository.findProductsExpiringSoon(today, futureDate);
    }

    // Get expired products
    @Transactional(readOnly = true)
    public List<Product> getExpiredProducts() {
        return productRepository.findExpiredProducts(LocalDate.now());
    }

    // Advanced search with filters
    @Transactional(readOnly = true)
    public Page<Product> searchProductsWithFilters(Long categoryId, Long supplierId,
                                                   ProductStatus status, BigDecimal minPrice,
                                                   BigDecimal maxPrice, String searchTerm,
                                                   Pageable pageable) {
        return productRepository.findByFilters(categoryId, supplierId, status,
                minPrice, maxPrice, searchTerm, pageable);
    }
}