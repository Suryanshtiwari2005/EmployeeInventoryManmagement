package com.inventoryEmployee.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String code;

    @Column(length = 255)
    private String imageUrl;

    // Self-referencing for parent category
    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "employees","products"})
    private Category parentCategory;

    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Product> products;

    @OneToMany(mappedBy = "parentCategory")
    @JsonIgnore
    private List<Category> subCategories;
}
