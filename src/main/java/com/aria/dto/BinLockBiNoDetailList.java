package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinLockBiNoDetailList {

    private Double lot;
    private String binId;
    private Long binName;
    private String campName;
    private String inwardDate;
    private String state;
    private String lorryserialNo;
    private String batchNo;
    private Long batchId;

    private Double qty;

    public BinLockBiNoDetailList(BinLockBiNoDetails binLockBiNoDetails) {
        DateUtil dateUtil = new DateUtil();
        this.lot = binLockBiNoDetails.getLot();
        this.binId = binLockBiNoDetails.getBinId();
        this.binName = binLockBiNoDetails.getBinName();
        this.campName = binLockBiNoDetails.getCampName();
        this.inwardDate = dateUtil.getDateTOStringWk(binLockBiNoDetails.getInwardDate());
        this.state = binLockBiNoDetails.getState();
        this.lorryserialNo = binLockBiNoDetails.getLorryserialNo();
        this.batchNo = binLockBiNoDetails.getBatchNo();
        this.batchId = binLockBiNoDetails.getBatchId();
        this.qty = binLockBiNoDetails.getQty();
    }
}
