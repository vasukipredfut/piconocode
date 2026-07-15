package com.aria.dto;


import com.aria.model.BinLockDetails;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinLockDetailsDto {

    private Long uniqueId;
    private String binId;
    private String binName;
    private Long productId;
    private String productName;
    private Double qty;
    private String batchNo;
    private String batchId;
    private String lorrySerialNo;
    private String inWardDate;
    private Long campId;
    private String campName;
    private String stateName;
    private Long stateId;


    public BinLockDetailsDto(BinLockDetails binLockDetails) {
        this.uniqueId = binLockDetails.getUniqueId();
        this.binId = binLockDetails.getBinId();
        this.binName = binLockDetails.getBinName();
        this.productId = binLockDetails.getProductId();
        this.productName = binLockDetails.getProductName();
        this.qty = binLockDetails.getQty();
        this.batchNo = binLockDetails.getBatchNo();
        this.batchId = binLockDetails.getBatchId();
        this.lorrySerialNo = binLockDetails.getLorrySerialNo();
        DateUtil dateUtil = new DateUtil();
        if (binLockDetails.getInWardDate() != null) {
            this.inWardDate = dateUtil.getDateToStringDDMMYYYY(binLockDetails.getInWardDate());
        }
        this.campId = binLockDetails.getCampId();
        this.campName = binLockDetails.getCampName();
        this.stateName = binLockDetails.getStateName();
        this.stateId = binLockDetails.getStateId();
    }
}
