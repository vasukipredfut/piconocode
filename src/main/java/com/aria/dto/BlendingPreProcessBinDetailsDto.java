package com.aria.dto;

import com.aria.model.BlendingPreProcessBinDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BlendingPreProcessBinDetailsDto {

    private Long id;

    private Long blendingAdvicePreprocessId;

    private Long binNo;
    private String batchNo;
    private Long batchId;
    private Double lot;

    public BlendingPreProcessBinDetailsDto(BlendingPreProcessBinDetails binDetails) {
        this.id = binDetails.getId();
        this.blendingAdvicePreprocessId = binDetails.getBlendingAdvicePreprocessId();
        this.binNo = binDetails.getBinNo();
        this.batchNo = binDetails.getBatchNo();
        this.batchId = binDetails.getBatchId();
        this.lot = binDetails.getLot();
    }
}
