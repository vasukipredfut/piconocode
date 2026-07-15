package com.aria.dto;

import com.aria.model.Lot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductsWithLotDto {

     public Long productId;

     public String productName;
     public String splitConfigbased;

     public List<Lot> lotList;

     public ProductsWithLotDto(ProductsWithLotList products) {
          this.productId = products.getProductId();
          this.productName = products.getProductName();
          this.splitConfigbased = products.getSplitConfigbased();
     }
}
