package com.aria.dto;

import com.aria.model.BillReceive;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class BillReceiveDto {

    private Long id;

    private String billNumber;

    private String billDate;

    private Long fromBranch;

    private String fromBranchName;

    private String billInwardDate;

    private Long fromPerson;

    private String fromPersonName;

    private Long fromDepartment;

    private String fromDepartmentName;

    private Long vendorId;

    private String vendorName;

    private String status;

    private String forwardDate;

    private String receivedDate;

    private String exchangeDate;

    private Long receivedPerson;

    private String receivedPersonName;

    private Long receivedBranch;

    private String receivedBranchName;

    private String mdStatus;

    private String mdapprovalDate;

    private String billCopy;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<BillReceiveProductDto> billDetails;

    private Long firmOrgId;


    public BillReceiveDto(BillReceive billreceive) {

        this.id = billreceive.getId();
        this.billNumber = billreceive.getBillNumber();
        if (billreceive.getFromBranch() != null) {
            this.fromBranch = billreceive.getFromBranch().getId();
            this.fromBranchName = billreceive.getFromBranch().getBranchName();
        }
        if (billreceive.getFromPerson() != null) {
            this.fromPerson = billreceive.getFromPerson().getId();
            this.fromPersonName = billreceive.getFromPerson().getEmployeename();
            this.fromDepartment = billreceive.getFromPerson().getDepartment().getId();
            this.fromDepartmentName = billreceive.getFromPerson().getDepartment().getName();
        }
        if (billreceive.getReceivedPerson() != null) {
            this.receivedPerson = billreceive.getReceivedPerson().getId();
            this.receivedPersonName = billreceive.getReceivedPerson().getEmployeename();
        }
        if (billreceive.getReceivedBranch() != null) {
            this.receivedBranch = billreceive.getReceivedBranch().getId();
            this.receivedBranchName = billreceive.getReceivedBranch().getBranchName();
        }
        if (billreceive.getVendor() != null) {
            this.vendorId = billreceive.getVendor().getId();
            this.vendorName = billreceive.getVendor().getName();
        }
        this.status = billreceive.getStatus();
        this.createdBy = billreceive.getCreatedBy();
        this.updatedBy = billreceive.getUpdatedBy();
        this.created_date = billreceive.getCreated_date();
        this.updated_date = billreceive.getUpdated_date();
        DateUtil dateUtil = new DateUtil();
        if (billreceive.getMdapprovalDate() != null) {
            this.mdapprovalDate = dateUtil.getDateToStringDDMMYYYY(billreceive.getMdapprovalDate());
        }
        this.mdStatus = billreceive.getMdStatus();
        this.firmOrgId = billreceive.getFirm_org_id();

        this.billDetails = billreceive.getBillReceiveProducts() != null
                ? billreceive.getBillReceiveProducts().stream().map(field -> new BillReceiveProductDto(field)).collect(Collectors.toList()) : null;

    }
}