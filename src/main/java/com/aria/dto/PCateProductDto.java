package com.aria.dto;

import com.aria.dao.PCateProduct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PCateProductDto {


    private  String name;
    private String dispName;
    private Long productId;
    private Double lot;




    public PCateProductDto(PCateProduct pCateProduct) {
        this.name = pCateProduct.getName();
        this.dispName = pCateProduct.getDispName();
        this.productId = pCateProduct.getProductId();
        this.lot = pCateProduct.getLot();
    }
}
