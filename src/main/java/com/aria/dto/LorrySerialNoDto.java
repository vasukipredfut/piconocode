package com.aria.dto;

import com.aria.model.BillInward;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LorrySerialNoDto {

    private Long hoBillId;

    private Long id;

    private String receivedDate;

    private String billDate;

    private String billNo;

    private String lorrySerialNo ;

    private String lorryno ;

    int lorryNoWithCount;


    public LorrySerialNoDto(BillInward billInward) {
        DateUtil dateUtil = new DateUtil();
        this.hoBillId = billInward.getHoBillId();
        this.id = billInward.getId();
        this.receivedDate = dateUtil.getDateTOStringWk(billInward.getReceivedDate());
        this.billDate = dateUtil.getDateTOStringWk(billInward.getBillDate());
        this.billNo = billInward.getBillNo();
        this.lorrySerialNo = billInward.getLorrySerialNo();
        this.lorryno = billInward.getLorryno();

    }
}
