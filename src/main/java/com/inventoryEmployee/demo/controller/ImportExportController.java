package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Product;
import com.inventoryEmployee.demo.service.EmployeeService;
import com.inventoryEmployee.demo.service.ImportExportService;
import com.inventoryEmployee.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/import-export")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImportExportController {

    private final ImportExportService importExportService;
    private final ProductService productService;
    private final EmployeeService employeeService;

    // Import products from CSV
    @PostMapping("/import/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Product>> importProducts(@RequestParam("file") MultipartFile file) {
        List<Product> products = importExportService.importProductsFromCSV(file);
        return ResponseEntity.ok(products);
    }

    // Export products to CSV
    @GetMapping("/export/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InputStreamResource> exportProducts() {
        List<Product> products = productService.getAllProducts(PageRequest.of(0, 10000)).getContent();
        ByteArrayInputStream data = importExportService.exportProductsToCSV(products);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=products.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(data));
    }

    // Export employees to CSV
    @GetMapping("/export/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> exportEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        ByteArrayInputStream data = importExportService.exportEmployeesToCSV(employees);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=employees.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(data));
    }
}
