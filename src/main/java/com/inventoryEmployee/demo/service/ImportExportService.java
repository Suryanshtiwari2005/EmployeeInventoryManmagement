package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Product;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportExportService {

    private final ProductService productService;
    private final EmployeeService employeeService;

    // Import products from CSV
    public List<Product> importProductsFromCSV(MultipartFile file) {
        List<Product> importedProducts = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                // FIX: Check if the row has enough columns (we need at least 4: Name, SKU, Desc, Price)
                if (row.length < 4) {
                    log.warn("Skipping invalid or empty row at line {}: {}", i + 1, java.util.Arrays.toString(row));
                    continue;
                }

                // Optional: Handle empty strings to avoid parsing errors later
                String priceStr = row[3];
                if (priceStr == null || priceStr.trim().isEmpty()) {
                    log.warn("Skipping row at line {} due to missing price", i + 1);
                    continue;
                }

                Product product = Product.builder()
                        .name(row[0])
                        .sku(row[1])
                        .description(row[2])
                        .price(new BigDecimal(row[3]))
                        .build();

                Product saved = productService.createProduct(product);
                importedProducts.add(saved);
            }

            log.info("Imported {} products from CSV", importedProducts.size());

        } catch (Exception e) {
            log.error("Error importing products from CSV", e);
            throw new RuntimeException("Failed to import products: " + e.getMessage());
        }

        return importedProducts;
    }

    // Export products to CSV
    public ByteArrayInputStream exportProductsToCSV(List<Product> products) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {

            // Write header
            String[] header = {"Name", "SKU", "Description", "Price", "Category", "Supplier"};
            writer.writeNext(header);

            // Write data
            for (Product product : products) {
                String[] row = {
                        product.getName(),
                        product.getSku(),
                        product.getDescription(),
                        product.getPrice().toString(),
                        product.getCategory() != null ? product.getCategory().getName() : "",
                        product.getSupplier() != null ? product.getSupplier().getName() : ""
                };
                writer.writeNext(row);
            }

            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error exporting products to CSV", e);
            throw new RuntimeException("Failed to export products: " + e.getMessage());
        }
    }

    // Export employees to CSV
    public ByteArrayInputStream exportEmployeesToCSV(List<Employee> employees) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {

            // Write header
            String[] header = {"First Name", "Last Name", "Email", "Phone", "Position",
                    "Department", "Salary", "Hire Date"};
            writer.writeNext(header);

            // Write data
            for (Employee employee : employees) {
                String[] row = {
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getEmail(),
                        employee.getPhone(),
                        employee.getPosition(),
                        employee.getDepartment() != null ? employee.getDepartment().getName() : "",
                        employee.getSalary() != null ? employee.getSalary().toString() : "",
                        employee.getHireDate() != null ? employee.getHireDate().toString() : ""
                };
                writer.writeNext(row);
            }

            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error exporting employees to CSV", e);
            throw new RuntimeException("Failed to export employees: " + e.getMessage());
        }
    }
}
