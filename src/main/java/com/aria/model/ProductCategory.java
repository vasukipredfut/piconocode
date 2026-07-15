package com.aria.model;

import com.aria.dto.ProductCategoryDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product_category")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long code;

    @Column
    private String name;

    public ProductCategory(ProductCategoryDto productCategory) {
        this.code = productCategory.getCode();
        this.name = productCategory.getName();
    }
}
