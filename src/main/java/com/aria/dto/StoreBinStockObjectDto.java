package com.aria.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class StoreBinStockObjectDto {

    private Long itemType;
    private Long itemId;
    private String itemName;

    private String colorName;
    private String campName;

    private String batchNo;
    private Long batchId;

    private Double billAmount;
    private String packageType;

    private String totalQuantity;

    private Long poId;
    private String poNumber;

    private Long billRefId;
   
    private String lot;


    private String binNo;
    private Long binId;
    private Double allottedQuantity;

}
