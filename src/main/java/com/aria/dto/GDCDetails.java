package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GDCDetails {

    private Company companyDetails;
    private BranchAddress fromBranchDetails;
    private BranchAddress issueBranchDetails;

    public String gdcNo;
    public Long issueBranchId;
    public Long requestBranchId;
    public Long requestId;
    public Long issueId;

    public Long updatedBy;
    public String updatedByName;

    public Long senderId;
    public Long deliveryUserId;
    public String transportMode;
    public String issueMode;
    public String truckNo;
    public String truckType;

    public String date;

    public String dateTime;
    private String deliveryByStaffName;
    private String senderName;

    public String gdcDate;
    public String gdcDateTime;

    List<GDCItems> gdcItemsList;

    public GDCDetails(GDCDetailsList gdcDetails) {
        DateUtil dateUtil = new DateUtil();
        this.gdcNo = gdcDetails.getGdcNo();
        this.issueBranchId = gdcDetails.getIssueBranchId();
        this.requestBranchId = gdcDetails.getRequestBranchId();
        this.requestId = gdcDetails.getRequestId();
        this.updatedBy = gdcDetails.getUpdatedBy();
        this.issueId = gdcDetails.getIssueId();
        this.senderId = gdcDetails.getSenderId();
        this.deliveryUserId = gdcDetails.getDeliveryUserId();
        this.transportMode = gdcDetails.getTransportMode();
        this.issueMode = gdcDetails.getIssueMode();
        this.truckNo = gdcDetails.getTruckNo();
        this.truckType = gdcDetails.getTruckType();
        this.date = dateUtil.getDateTOStringWk(gdcDetails.getDispatchOn());
        this.dateTime = dateUtil.getDateTime(gdcDetails.getDispatchOn());
        if (gdcDetails.getGdcDate() != null) {
            this.gdcDate = dateUtil.getLocalDateTimeToDateString(gdcDetails.getGdcDate());
            this.gdcDateTime = dateUtil.getLocalDateTimeToDateTimeString(gdcDetails.getGdcDate());
        }

    }
}
