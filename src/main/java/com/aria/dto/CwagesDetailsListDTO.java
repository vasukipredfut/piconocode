package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

@Getter
@Setter
@NoArgsConstructor
public class CwagesDetailsListDTO {


    private String trnId;
    private String trnNo;
    private String cwagesId;
    private String wagesWorkName;
    private String codeNo;
    private String refTrnNo;
    private String productId;
    private String productName;
    private String unitRate;
    private String qty;



    public CwagesDetailsListDTO(CwagesDetailsList cwagesDetailsList) {
        this.trnId = cwagesDetailsList.getTrnId();
        this.trnNo = cwagesDetailsList.getTrnNo();
        this.cwagesId = cwagesDetailsList.getCwagesId();
        this.wagesWorkName = StringEscapeUtils.unescapeHtml4(cwagesDetailsList.getWagesWorkName());
        this.codeNo = cwagesDetailsList.getCodeNo();
        this.refTrnNo = cwagesDetailsList.getRefTrnNo();
        this.productId = cwagesDetailsList.getProductId();
        this.productName = cwagesDetailsList.getProductName();
        this.unitRate = cwagesDetailsList.getUnitRate();
        this.qty = cwagesDetailsList.getQty();
    }
}
