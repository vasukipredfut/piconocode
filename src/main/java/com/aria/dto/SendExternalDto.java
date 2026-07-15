package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SendExternalDto {

    private Long materialFlowSendExternalId;
    private String transactionNumber;
    private String referenceDate;
    private long activeFlag;
    private String issueMode;
    private Long issueById;
    private String materialReturn;
    private String toReach;
    private Long toVendorRefId;
    private PartyDetails vendorDetails;
    private Long toDepartmentId;
    private Long toBranchId;
    private Long createdDepartmentId;
    private Long createdBranchId;
    private Long status;
    private String vehicle;
    private String vehicleNumber;
    private String issueByName;
    private String toDepartmentName;

    private String createdDepartmentName;

    private BranchAddress fromBranchDetails;

    private Long createdBy;
    private String createdByName;

    private BranchAddress toBranchDetails;

    private Company firmDetails;

    private String GDCNO;
    private String gdcDate;
    private String gdcDateTime;
    private String ewayBill;

    private Long firmId;

    private List<SendExternalItemDto> sendExternalItems;

}
