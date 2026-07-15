package com.aria.dto;

import com.aria.model.ProductList;
import com.aria.model.ProductMaster;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDropDownDto {


    private Long id;

    private String productName;

  


    public ProductDropDownDto(ProductMaster productList) {
 
    	this.id = productList.getId();
    	this.productName=productList.getProductName();
    }
}
