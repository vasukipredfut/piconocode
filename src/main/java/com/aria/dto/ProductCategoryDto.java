package com.aria.dto;

import com.aria.model.ProductCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class ProductCategoryDto {

    private Long code;

    private String name;

    public ProductCategoryDto(ProductCategory productCategory) {
        this.code = productCategory.getCode();
        this.name = productCategory.getName();
    }
}
