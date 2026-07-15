package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.SalesReturn;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;




@Getter
@Setter
@NoArgsConstructor
public class SalesReturnDto {

    private Long id;

    private Long firmId;

    private String firmName;

    private Long vendorId;

    private String vendorName;

    private String referenceNo;

    private String billDate;

    private String trnNo;

    private String trnDate;

    private double amount;

    private Long vid;

    private String von;

    private String con;

    private Long verifyBy;

    private Long branch;

    private Long dept;

    private Long createdBy;

    private Long updatedBy;
    private Long verifiedBy;
    private Long rejectedBy;



    private List<SalesReturnProductsDto> salesReturnProducts;

    private String returnReason;

    private String returnType;

    private String status;



    private String createdByName;
    private String createdDate;
    private String createdDateTime;

    private String updatedByName;
    private String updatedDate;
    private String updatedDateTime;

    private String verifyByName;
    private String verifiedDate;
    private String verifiedDateTime;

    private String rejectedByName;
    private String rejectedDate;
    private String rejectedDateTime;


    public SalesReturnDto(SalesReturn salesReturn) {

        DateUtil dateUtil = new DateUtil();

        this.id = salesReturn.getId();
        if (salesReturn.getFirmOrg() != null) {
            this.firmId = salesReturn.getFirmOrg().getId();
            this.firmName = salesReturn.getFirmOrg().getName();
        }

        if (salesReturn.getVendor() != null) {
            this.vendorId = salesReturn.getVendor().getId();
            this.vendorName = salesReturn.getVendor().getName();
        }
        this.referenceNo = salesReturn.getReferenceNo();

        this.billDate = dateUtil.getDateTOStringWk(salesReturn.getBillDate());

        this.trnNo = salesReturn.getTrnNo();

        this.trnDate = dateUtil.getDateTOStringWk(salesReturn.getTrnDate());

        this.amount = salesReturn.getAmount();
        this.vid = salesReturn.getVid();
        this.von = salesReturn.getVon();
        this.con = salesReturn.getCon();
        this.dept = salesReturn.getDept();
        this.branch = salesReturn.getBranch();

        this.createdBy = salesReturn.getCreatedBy();
        this.updatedBy = salesReturn.getUpdatedBy();

        this.createdDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(salesReturn.getCreated_date());
        this.createdDateTime = dateUtil.localDateTimeToString(salesReturn.getCreated_date());

        if (salesReturn.getUpdated_date() != null) {
            this.updatedDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(salesReturn.getUpdated_date());
            this.updatedDateTime = dateUtil.localDateTimeToString(salesReturn.getUpdated_date());
        }

        this.returnReason = salesReturn.getReturn_reason();
        this.returnType = salesReturn.getReturn_type();
        this.status = salesReturn.getStatus();
        this.verifiedBy = salesReturn.getVerifiedBy();
        this.rejectedBy = salesReturn.getRejectedBy();
        if (salesReturn.getVerified_date() != null) {
            this.verifiedDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(salesReturn.getVerified_date());
            this.verifiedDateTime = dateUtil.localDateTimeToString(salesReturn.getVerified_date());
        }

        if (salesReturn.getRejected_date() != null) {
            this.rejectedDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(salesReturn.getRejected_date());
            this.rejectedDateTime = dateUtil.localDateTimeToString(salesReturn.getRejected_date());
        }

        this.salesReturnProducts = salesReturn.getSalesReturnProducts() != null
                ? salesReturn.getSalesReturnProducts().stream().map(field -> new SalesReturnProductsDto(field)).collect(Collectors.toList()) : null;

    }

}
