package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MrnPendingDTO {

    private Long firmId;
    private String firmName;
    private String trnNo;
    private String trnDate;
    private Long requestBranchId;
    private String requestBranchName;
    private Long issueBranchId;
    private String issueBranchName;
    private Long requestDeptId;
    private String requestDeptName;
    private Long issueDeptId;
    private String issueDeptName;
    private String productName;
    private String qty;
    private String lot;
    private String batchNo;
    private String lockstatus;


    private String status;
    private String personName;
    private String date;
    private String dateTime;


    public MrnPendingDTO(MrnPendingList pendingList) {
        DateUtil dateUtil = new DateUtil();
        this.firmId = pendingList.getFirmId();
        this.firmName = pendingList.getFirmName();
        this.trnNo = pendingList.getTrnNo();
        this.trnDate = dateUtil.getDateTOStringWk(pendingList.getTrnDate());
        this.requestBranchId = pendingList.getRequestBranchId();
        this.requestBranchName = pendingList.getRequestBranchName();
        this.issueBranchId = pendingList.getIssueBranchId();
        this.issueBranchName = pendingList.getIssueBranchName();
        this.requestDeptId = pendingList.getRequestDeptId();
        this.requestDeptName = pendingList.getRequestDeptName();
        this.issueDeptId = pendingList.getIssueDeptId();
        this.issueDeptName = pendingList.getIssueDeptName();
        this.productName = pendingList.getProductName();
        this.qty = pendingList.getQty();
        this.lot = pendingList.getLot();
        this.batchNo = pendingList.getBatchNo();
        this.lockstatus = pendingList.getLockstatus();
    }
}
