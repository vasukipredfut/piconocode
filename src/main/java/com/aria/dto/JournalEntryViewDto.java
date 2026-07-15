package com.aria.dto;


import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JournalEntryViewDto {


    private String trnDate;
    private String trnNo;
    private Long wcid;
    private String wcTable;
    private String workCenterName;
    private Long aby;
    private String accountingByName;
    private Long eby;
    private String entryByName;
    private String entryOn;
    private String producedProductName;
    private Long productId;
    private Long journalId;
    private String journalName;
    private String shiftName;
    private String departmentName;
    private String branchName;
    private Long godownId;
    private String godownName;
    private String status;

    List<JournalEntryDto> outputProducts;

    public JournalEntryViewDto(JournalEntryView journalEntryView) {
        DateUtil dateUtil = new DateUtil();
        this.trnDate = dateUtil.getDateTOStringWk(journalEntryView.getTrnDate());
        this.trnNo = journalEntryView.getTrnNo();
        this.wcid = journalEntryView.getWcid();
        this.wcTable = journalEntryView.getWcTable();
        this.godownId = journalEntryView.getGodownId();
        this.producedProductName = journalEntryView.getProducedProductName();
        this.productId = journalEntryView.getProductId();
        this.journalId = journalEntryView.getJournalId();
        this.journalName = journalEntryView.getJournalName();
        this.shiftName = journalEntryView.getShiftName();
        this.departmentName = journalEntryView.getDepartmentName();
        this.branchName = journalEntryView.getBranchName();
        this.aby = journalEntryView.getAby();
        this.eby = journalEntryView.getEby();
        this.entryOn = dateUtil.getDateTime(journalEntryView.getADate());
        if(journalEntryView.getStatus() != null && journalEntryView.getStatus().contains("A")) {
            this.status = "Accounted";
        }
    }
}
