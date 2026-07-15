package com.aria.dto;


import com.aria.model.BillForward;
import com.aria.model.BillForwardProduct;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class BillForwardDto {

    private Long id;

    private Long billInwardId;
    private Long firmOrgId;
    private String billNumber;

    private String billDate;
    
    private Long fromBranch;

    private String fromBranchName;

    private Long toBranch;

    private String toBranchName;

    private String billInwardDate;

    private Long fromPerson;

    private String fromPersonName;
    
    private Long vendorId;

    private String vendorName;

    private String forwardDate;

    private String status;

    private Long createdBy;

    private Long updatedBy;

    private Long hardCopyHandOverToUser;

    private String hardCopyHandOverToUserName;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private Long billApprovedBy;
   
    private String billApprovedDate;

    private Long billVerifiedBy;

    private String billVerifiedDate;

    protected List<BillForwardProductDto> billDetails;

    private String lorryWithWeight;

    private String lorryWithoutWeight;

    private String lorrySerialNo;

    private Double billAmount;

    private String billAmountStringValue;


    private Boolean excessOrShortage;

    private String shexStatus;

    private String productCategory;
    private Long trnNo;
    public BillForwardDto(BillForward billForward) {
        DateUtil dateUtil = new DateUtil();
        this.id = billForward.getId();
        this.firmOrgId = billForward.getFirm_org_id();
        this.billInwardId = billForward.getBill_ref_id();
        this.billNumber = billForward.getBillNumber();
        if(billForward.getFromBranch() != null) {
            this.fromBranch = billForward.getFromBranch().getId();
            this.fromBranchName = billForward.getFromBranch().getBranchName();
        }
        if(billForward.getToBranch() != null) {
            this.toBranch = billForward.getToBranch().getId();
            this.toBranchName = billForward.getToBranch().getBranchName();
        }
        if(billForward.getFromPerson() != null) {
            this.fromPerson = billForward.getFromPerson().getId();
            this.fromPersonName = billForward.getFromPerson().getEmployeename();
        }
		if (billForward.getVendor() != null) {
			this.vendorId = billForward.getVendor().getId();
			this.vendorName = billForward.getVendor().getName();
		}
        if(billForward.getHardCopyHandOverToUser() != null) {
            this.hardCopyHandOverToUser = billForward.getHardCopyHandOverToUser().getId();
            this.hardCopyHandOverToUserName = billForward.getHardCopyHandOverToUser().getEmployeename();
        }
        this.status = billForward.getStatus();
        this.createdBy = billForward.getCreatedBy();
        this.updatedBy = billForward.getUpdatedBy();
        this.created_date = billForward.getCreated_date();
        this.updated_date = billForward.getUpdated_date();
        this.billApprovedBy = billForward.getBillApprovedBy();
        this.trnNo = billForward.getTrn_no();
        if(billForward.getBill_approved_date() != null) {
        this.billApprovedDate = dateUtil.getDateTOStringWk(billForward.getBill_approved_date());
        }
        this.billVerifiedBy = billForward.getBill_verified_by();
        this.billVerifiedDate = billForward.getBill_verified_date()!=null?dateUtil.getDateTOStringWk(billForward.getBill_verified_date()):null;

        this.billDetails = billForward.getBillForwardProducts() != null
                ? billForward.getBillForwardProducts().stream().map(field -> new BillForwardProductDto(field)).collect(Collectors.toList()) : null;


    }
}
