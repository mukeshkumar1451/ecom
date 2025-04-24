package com.ecommerce.project.modal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    @NotBlank
    @Size(min = 3, message = "Product name must contain at least 3 characters")
    private String productName;
    private String image;
    @NotBlank
    @Size(min = 6, message = "Product description must contain at least 6 characters")
    private String description;
    private double price;
    private double discount;
    private Integer quantity;
    private double specialPrice;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
