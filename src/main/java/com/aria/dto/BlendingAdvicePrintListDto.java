package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BlendingAdvicePrintListDto {

    public String journalName;

    public String id;
    public String adviceId;

    public String fromDate;

    public String toDate;

    public String productId;

    public String productName;

    public String status;
    public String week;

    public String casTrn;
    public String branchName;

    public BlendingAdvicePrintListDto(BlendingAdvicePrintList printList) {
        DateUtil dateUtil = new DateUtil();
        this.journalName = printList.getJournalName();
        this.id = printList.getId();
        this.adviceId = printList.getAdviceId();
        if (printList.getFromDate() != null) {
            this.fromDate = dateUtil.getDateTOStringWk(printList.getFromDate());
        }
        if (printList.getFromDate() != null) {
            this.toDate = dateUtil.getDateTOStringWk(printList.getToDate());
        }
        this.productId = printList.getProductId();
        this.productName = printList.getProductName();
        this.branchName = printList.getBranchName();
        this.status = printList.getStatus();
        this.casTrn = printList.getCasTrn();

        if (fromDate != null && toDate != null) {
            this.week = fromDate + " to " + toDate;
        }
    }
}
