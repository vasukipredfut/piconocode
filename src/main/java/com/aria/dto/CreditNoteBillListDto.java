package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@NoArgsConstructor
@Getter
@Setter
public class CreditNoteBillListDto {


    private Long vendorId;
    private String inwardDate;
    private String billNo;
    private String billDate;

    public CreditNoteBillListDto(CreditNoteBillList billList) {
        DateUtil dateUtil = new DateUtil();
        this.vendorId = billList.getVendorId();
        this.billNo = billList.getBillNo();
        if (billList.getInwardDate() != null)
            this.inwardDate = dateUtil.getDateTOStringWk(billList.getInwardDate());

        if (billList.getBillDate() != null)
            this.billDate = dateUtil.getDateTOStringWk(billList.getBillDate());

    }
}
