package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductionBatchListDto {

    private Long productId;
    private Long batchId;

    private String batchNo;

    private String inwardDate;

    public ProductionBatchListDto(ProductionBatchList productionBatchList) {
        DateUtil dateUtil = new DateUtil();
        this.productId = productionBatchList.getProductId();
        this.batchId = productionBatchList.getBatchId();
        this.batchNo = productionBatchList.getBatchNo();
        if (productionBatchList.getInwardDate() != null) {
            this.inwardDate = dateUtil.getDateTOStringWk(productionBatchList.getInwardDate());
        }
    }
}
