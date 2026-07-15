package com.aria.dto;

import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductExcessStorageListDto {


    private Long id;

    private Long vendorId;

    private String vendorName;

    private String productName;

    private String billInwardDate;

    private String type;

    private String billNo;
    
    private String status;


    public ProductExcessStorageListDto(ProductExcessStorageList productExcessStorageList) {
        DateUtil dateUtil = new DateUtil();
        this.id = productExcessStorageList.getId();
        this.vendorId = productExcessStorageList.getVendorId();
        this.vendorName = productExcessStorageList.getVendorName();
        this.productName = productExcessStorageList.getProductName();
        this.billInwardDate = dateUtil.getDateTOStringWk(productExcessStorageList.getBillInwardDate());
        this.type = productExcessStorageList.getType();
        this.billNo = productExcessStorageList.getBillNo();
        this.status = productExcessStorageList.getStatus();
    }
}
