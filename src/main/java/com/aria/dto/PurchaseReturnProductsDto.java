package com.aria.dto;

import com.aria.model.PurchaseReturnProducts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnProductsDto {

    private Long id;

    private Long purchaseReturnId;

    private Long productId;

    //private String productCode;

    private String productName;

    private String productType;

    private String batchNo;

    private String currentStock;

    private String lot;

    private String bin;

    private String returnQuantity;

    private String reason;

	 private String isSelect;

    private List<ExcessStoredBinDetailsDto> binDetails;

    public PurchaseReturnProductsDto(PurchaseReturnProducts purchaseReturnProducts) {
        this.id = purchaseReturnProducts.getId();
        this.purchaseReturnId = purchaseReturnProducts.getPurchaseReturnId();
        if (purchaseReturnProducts.getProduct() != null) {
            this.productId = purchaseReturnProducts.getProduct().getId();
            this.productName = purchaseReturnProducts.getProduct().getProductName();
        }
        //this.productCode = purchaseReturnProducts.getProductCode();
        this.productType = purchaseReturnProducts.getProductType();
        this.batchNo = purchaseReturnProducts.getBatchNo();
        this.lot = purchaseReturnProducts.getLot();
        this.bin = purchaseReturnProducts.getBin();
        this.currentStock = purchaseReturnProducts.getCurrentStock();
        this.returnQuantity = purchaseReturnProducts.getReturnQuantity();
        this.reason = purchaseReturnProducts.getReason();
        this.isSelect = purchaseReturnProducts.getIsSelect();
        this.binDetails = purchaseReturnProducts.getBinDetails() != null
                ? purchaseReturnProducts.getBinDetails().stream().map(field -> new ExcessStoredBinDetailsDto(field)).collect(Collectors.toList()) : null;
    }
}