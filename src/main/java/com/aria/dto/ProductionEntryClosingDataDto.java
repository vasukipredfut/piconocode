package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryClosingDataDto {


    private String casgroup;
    private Long blendingStockId;
    private String blendingStockType;
    private Long productId;
    private String productName;
    private String category;
    private String binNo;
    private String quantity;
    private String lot;
    private String totalInputQuantity;
    private String totalOutputQuantity;
    private String deductBatchNo;
    private String bomIntakeType;

    // need for validation response

    private String status;
    private String message;
    private String actualQty;

}
