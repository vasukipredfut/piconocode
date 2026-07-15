package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CwagesDetDTO {


    private String refTable;
    private String refTrnNo;
    private String productId;
    private String productName;


    private String billNo;
    private String billDate;
    private String lorryDate;
    private String lorrySerialNo;
    private String voucherType;
    private String vendorName;


    private String workCenterName;
    private String journalName;
    private String batchNo;
    private String trnProduct;


    public CwagesDetDTO(CwagesDet cwagesDet) {
        this.refTable = cwagesDet.getRefTable();
        this.refTrnNo = cwagesDet.getRefTrnNo();
        this.productId = cwagesDet.getProductId();
        this.productName = cwagesDet.getProductName();
        this.trnProduct = cwagesDet.getProductName();
    }

    List<CwagesDetailsListDTO> cwagesDetailsList;
}
