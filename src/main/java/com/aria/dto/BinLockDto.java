package com.aria.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.aria.model.BinLock;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BinLockDto {

    private Long id;

    private String binNo;

    private String lockType;

    private String lockDate;

    private String lockStatus;

    private Long lockedEmployeeId;

    private String lockedEmployeeName;

    private Long productId;

    private String productName;

    private String batchNo;

    private Long batchId;

    private String lorrySerialNo;

    private String inwardDate;


    private String state;

    private Double quantity;

    private Long createdBy;
    private String createdDateString;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    public BinLockDto(BinLock binLock) {

        this.id = binLock.getId();
        this.binNo = binLock.getBinNo();
        this.lockType = binLock.getLockType();
        if (binLock.getLockedEmployee() != null) {
            this.lockedEmployeeId = binLock.getLockedEmployee().getId();
            this.lockedEmployeeName = binLock.getLockedEmployee().getEmployeename();
        }
        if (binLock.getProduct() != null) {
            this.productId = binLock.getProduct().getId();
            this.productName = binLock.getProduct().getProductName();
        }
        this.batchNo = binLock.getBatchNo();
        this.batchId = binLock.getBatchId();
        this.lorrySerialNo = binLock.getLorrySerialNo();
        this.lockStatus = binLock.getLockStatus();

        DateUtil dateUtil = new DateUtil();
        if (binLock.getLockDate() != null) {
            this.lockDate = dateUtil.getDateTOStringWk(binLock.getLockDate());
        }
        if (binLock.getInwardDate() != null) {
            this.inwardDate = dateUtil.getDateTOStringWk(binLock.getInwardDate());
        }
        if (binLock.getCreated_date() != null) {
            this.createdDateString = dateUtil.getLocalDateTimeToStringDDMMYYYY1(binLock.getCreated_date());
        }
        this.state = binLock.getState();
        this.quantity = binLock.getQuantity();
        this.createdBy = binLock.getCreatedBy();
        this.updatedBy = binLock.getUpdatedBy();
        this.created_date = binLock.getCreated_date();
        this.updated_date = binLock.getUpdated_date();
    }
}
