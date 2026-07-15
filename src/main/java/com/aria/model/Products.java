package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.ProductsDto;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "products")
public class Products {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_type")
    private String productType;

    @Column
    private Long lot;

    @Column(name = "batch_no")
    private String batchNo;
    
    @Column
    private String bin;
    
    @Column(name = "trn_type")
    private String trnType;
    
    @Column
    private String type;
    
    @Column
    private String quantity;
    
    @Column
    private int active;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;



    public  Products( ProductsDto  products) {
        this.id = products.getId();
        this.vendorId = products.getVendorId();
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
        this.updated_date = products.getUpdated_date();
    }
}
