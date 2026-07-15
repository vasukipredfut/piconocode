package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierEvalValidUsersDTO {

    private String trnNo;
    private String status;
    private String createdDate;
    private String createdStaff;
    private String createdStaffName;

    private String approvedDate;
    private String approvalStaff;
    private String approvalStaffName;
    private String verifiedDate;
    private String verifiedStaff;
    private String verifiedStaffName;
    private String authorisedDate;
    private String authorisedStaff;
    private String authorisedStaffName;

    public SupplierEvalValidUsersDTO(SupplierEvalValidUsers supplierEvalValidUsers) {
        DateUtil dateUtil = new DateUtil();
        this.trnNo = supplierEvalValidUsers.getTrnNo();
        this.status = supplierEvalValidUsers.getStatus();
        this.createdDate = dateUtil.getDateTOStringWk(supplierEvalValidUsers.getCreatedDate());
        this.createdStaff = supplierEvalValidUsers.getCreatedStaff();
        this.createdStaffName = supplierEvalValidUsers.getCreatedStaffName();
        this.approvedDate = dateUtil.getDateTOStringWk(supplierEvalValidUsers.getApprovedDate());
        this.approvalStaff = supplierEvalValidUsers.getApprovalStaff();
        this.approvalStaffName = supplierEvalValidUsers.getApprovalStaffName();
        this.verifiedDate = dateUtil.getDateTOStringWk(supplierEvalValidUsers.getVerifiedDate());
        this.verifiedStaff = supplierEvalValidUsers.getVerifiedStaff();
        this.verifiedStaffName = supplierEvalValidUsers.getVerifiedStaffName();
        this.authorisedDate = dateUtil.getDateTOStringWk(supplierEvalValidUsers.getAuthorisedDate());
        this.authorisedStaff = supplierEvalValidUsers.getAuthorisedStaff();
        this.authorisedStaffName = supplierEvalValidUsers.getAuthorisedStaffName();
    }
}
