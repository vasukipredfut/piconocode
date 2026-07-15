package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.Products;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductsDto {

	private Long id;

   private Long vendorId;
   
    private String productName;

    
    private String productCode;

    
    private String productType;

    
    private Long lot;

    
    private String batchNo;
    
   
    private String bin;
    
    
    private String trnType;
    
    private String quantity;
    
    
    private String type;
  
    private int active;

   
    private Long createdBy;

    
    private Long updatedBy;

   
    private LocalDateTime created_date;

  
    private LocalDateTime updated_date;



    public  ProductsDto( Products  products) {
        this.id = products.getId();
        this.vendorId =products.getVendorId();
        this.productName = products.getProductName();
        this.productCode = products.getProductCode();
        this.productType = products.getProductType();
        this.lot = products.getLot();
        this.batchNo = products.getBatchNo();
        this.bin = products.getBin();
        this.trnType = products.getTrnType();
        this.quantity = products.getQuantity();
        this.active = products.getActive();
        this.type = products.getType();
        this.createdBy = products.getCreatedBy();
        this.updatedBy = products.getUpdatedBy();
        this.created_date = products.getCreated_date();
        if (products.getUpdated_date() != null) {
            this.updated_date = products.getUpdated_date();
        }else {
            this.updated_date = products.getCreated_date();
        }
    }
}
