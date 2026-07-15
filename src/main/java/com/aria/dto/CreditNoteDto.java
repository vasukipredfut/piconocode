package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CreditNoteDto {

    private Long id;

    private String creditNoteNumber;

    private String creditNoteBillNumber;

    private String type;
    private Long billType;

    private Double creditAmount;

    private Double creditQuantity;

    private String creditNoteDate;
    private String billInwardDate;
    private String status;
    private String vendorName;
    private Long vendorId;

    public CreditNoteDto(CreditNoteList creditNoteList) {
        DateUtil dateUtil = new DateUtil();
        this.id = creditNoteList.getId();
        this.creditNoteNumber = creditNoteList.getCreditNoteNumber();
        this.creditNoteBillNumber = creditNoteList.getCreditNoteBillNumber();
        this.type = creditNoteList.getType();
        this.creditAmount = creditNoteList.getCreditAmount();
        this.creditQuantity = creditNoteList.getCreditQuantity();
        this.billInwardDate = creditNoteList.getBillInwardDate();
        this.vendorId = creditNoteList.getVendorId();
        this.vendorName = creditNoteList.getVendorName();
        this.billType = creditNoteList.getBillType();
        this.creditNoteDate = dateUtil.getDateTOStringWk(creditNoteList.getCreditNoteDate());
        this.status = creditNoteList.getStatus();
    }

}
