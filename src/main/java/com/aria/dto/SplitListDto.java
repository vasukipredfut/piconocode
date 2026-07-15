package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SplitListDto {

    public String trnNo;
    public String trnDate;
    public String status;
    public double qty;
    public String productName;
    public Long productId;
    public Long binNo;
    public String batchNo;
    public Long batchId;
    public Long id;



    public SplitListDto(SplitList split) {
        DateUtil dateUtil = new DateUtil();
        this.id = split.getId();
        this.trnNo = split.getTrnNo();
        this.trnDate = dateUtil.getDateTOStringWk(split.getTrnDate());
        this.status = split.getStatus();
        if(split.getQty() != null) {
            this.qty = split.getQty();
        }else {
            this.qty = 0.0;
        }
        this.productName = split.getProductName();
        this.productId = split.getProductId();
        this.binNo = split.getBinNo();
        this.batchNo = split.getBatchNo();
        this.batchId = split.getBatchId();
    }
}
