package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JournalEntryListDto {

    
    private String status;

    private Long journalId;

    private String journalName;

    private String shiftDate;

    private Long shiftId;

    private String shiftName;
    
    private String trnNo;
    private String batchNo;

    public JournalEntryListDto(JournalEntryList journalEntryList) {
        DateUtil dateUtil = new DateUtil();
        if(journalEntryList.getStatus() != null && journalEntryList.getStatus().equalsIgnoreCase("A")) {
            this.status = "Accounted";
        }
        this.journalId = journalEntryList.getJournalId();
        this.journalName = journalEntryList.getJournalName();
        if (journalEntryList.getShiftDate() != null) {
            this.shiftDate = dateUtil.getDateTOStringWk(journalEntryList.getShiftDate());
        }
        this.shiftName = journalEntryList.getShiftName();
        this.shiftId = journalEntryList.getShiftId();
        this.trnNo = journalEntryList.getTrnNo();
        this.batchNo = journalEntryList.getBatchNo();

    }
}
