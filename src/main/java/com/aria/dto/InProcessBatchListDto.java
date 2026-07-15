package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InProcessBatchListDto {

    private Long batchId;
    private String batchNo;

    List<InProcessBatchWithDetailListDto> inProcessBatchWithDetailList;
    List<InProcessBatchWithDetailListDto> inProcessBatchWithBinList;

    public InProcessBatchListDto(InProcessBatchList inProcessBatchList) {
        this.batchId = inProcessBatchList.getBatchId();
        this.batchNo = inProcessBatchList.getBatchNo();
    }
}
