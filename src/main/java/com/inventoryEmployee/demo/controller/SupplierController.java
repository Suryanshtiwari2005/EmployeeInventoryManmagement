package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.entity.Supplier;
import com.inventoryEmployee.demo.repository.SupplierRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupplierController {

    private final SupplierRepository supplierRepository;

    // Create supplier
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody Supplier supplier) {
        Supplier created = supplierRepository.save(supplier);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Get supplier by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        return ResponseEntity.ok(supplier);
    }

    // Get all suppliers
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<Supplier>> getAllSuppliers(Pageable pageable) {
        Page<Supplier> suppliers = supplierRepository.findByDeletedFalse(pageable);
        return ResponseEntity.ok(suppliers);
    }

    // Update supplier
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id,
                                                   @Valid @RequestBody Supplier supplier) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        existing.setName(supplier.getName());
        existing.setContactPerson(supplier.getContactPerson());
        existing.setEmail(supplier.getEmail());
        existing.setPhone(supplier.getPhone());
        existing.setAddress(supplier.getAddress());
        existing.setCity(supplier.getCity());
        existing.setState(supplier.getState());
        existing.setCountry(supplier.getCountry());
        existing.setZipCode(supplier.getZipCode());
        existing.setTaxId(supplier.getTaxId());
        existing.setStatus(supplier.getStatus());

        Supplier updated = supplierRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    // Delete supplier (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
        return ResponseEntity.noContent().build();
    }
}
