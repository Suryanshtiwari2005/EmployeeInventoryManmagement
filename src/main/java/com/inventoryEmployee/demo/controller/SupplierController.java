package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.request.SupplierRequest;
import com.inventoryEmployee.demo.entity.Supplier;
import com.inventoryEmployee.demo.enums.SupplierStatus;
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
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody SupplierRequest request) {
        Supplier supplier = maptoEntity(request);

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
                                                   @Valid @RequestBody SupplierRequest request) {

        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        existing.setName(request.getName());
        existing.setContactPerson(request.getContactPerson());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setState(request.getState());
        existing.setCountry(request.getCountry());
        existing.setZipCode(request.getZipCode());
        existing.setTaxId(request.getTaxId());
        existing.setStatus(request.getStatus());

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

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

    private Supplier maptoEntity(SupplierRequest request){
        Supplier supplier = new Supplier();

        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAlternatePhone(request.getAlternatePhone());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setCountry(request.getCountry());
        supplier.setZipCode(request.getZipCode());
        supplier.setTaxId(request.getTaxId());
        supplier.setNotes(request.getNotes());
        supplier.setStatus(request.getStatus());

        if(request.getStatus() != null){
            supplier.setStatus(request.getStatus());
        }else{
            supplier.setStatus(SupplierStatus.ACTIVE);
        }

        return supplier;
    }
}
