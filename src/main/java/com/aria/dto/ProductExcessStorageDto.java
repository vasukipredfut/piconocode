package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.ProductExcessStorage;

@Getter
@Setter
@NoArgsConstructor
public class ProductExcessStorageDto {

    private Long id;

    private Long firmId;

    private String firmName;

    private Long vendorId;

    private String vendorName;

    private String billTransactionno;

    private String billDate;

    private String billInwardDate;

    private String billNo;

    private Long createdBy;
    private String createdByName;

    private Long updatedBy;
    private String updatedByName;

    private String createdDate;

    private String createdDateTime;

    private LocalDateTime updated_date;
    
    private String status;

    private String lorrySerialNo;

    private Long branchId;
    
    private List<ExcessShortageProductsDto> excessShortageProducts;

    private Long verifiedBy;
    private String verifiedByName;
    private String verifiedDate;
    private String verifiedDateTime;
    private Long rejectedBy;
    private String rejectedByName;
    private String rejectedDate;
    private String rejectedDateTime;

    public ProductExcessStorageDto(ProductExcessStorage productExcessStorage) {
        DateUtil dateUtil = new DateUtil();
        this.id = productExcessStorage.getId();
        if (productExcessStorage.getFirmOrg() != null) {
            this.firmId = productExcessStorage.getFirmOrg().getId();
            this.firmName = productExcessStorage.getFirmOrg().getName();
        }
        if (productExcessStorage.getVendor() != null) {
            this.vendorId = productExcessStorage.getVendor().getId();
            this.vendorName = productExcessStorage.getVendor().getName();
        }
        this.billNo = productExcessStorage.getBillNo();
        this.billTransactionno = productExcessStorage.getBillTransactionno();
        this.createdBy = productExcessStorage.getCreatedBy();
        this.updatedBy = productExcessStorage.getUpdatedBy();
        this.updated_date = productExcessStorage.getCreated_date();
        this.status = productExcessStorage.getStatus();
        this.lorrySerialNo = productExcessStorage.getLorrySerialNo();
        this.branchId = productExcessStorage.getBranchId();
        this.excessShortageProducts = productExcessStorage.getExcessShortageProducts() != null
                ? productExcessStorage.getExcessShortageProducts().stream()
                .map(field -> new ExcessShortageProductsDto(field)).collect(Collectors.toList())
                : null;
        this.verifiedBy = productExcessStorage.getVerifiedBy();
        this.rejectedBy = productExcessStorage.getRejectedBy();
        if (productExcessStorage.getCreated_date() != null) {
            this.createdDate = dateUtil.getLocalDateTimeToDateString(productExcessStorage.getCreated_date());
            this.createdDateTime = dateUtil.getLocalDateTimeToDateTimeString(productExcessStorage.getCreated_date());
        }
        if (productExcessStorage.getRejected_date() != null) {
            this.rejectedDate = dateUtil.getLocalDateTimeToDateString(productExcessStorage.getRejected_date());
            this.rejectedDateTime = dateUtil.getLocalDateTimeToDateTimeString(productExcessStorage.getRejected_date());
        }
        if (productExcessStorage.getVerified_date() != null) {
            this.verifiedDate = dateUtil.getLocalDateTimeToDateString(productExcessStorage.getVerified_date());
            this.verifiedDateTime = dateUtil.getLocalDateTimeToDateTimeString(productExcessStorage.getVerified_date());
        }

    }
}
