package com.aria.dto;


import com.aria.model.BlendingAdvicePreProcess;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class BlendingAdvicePreProcessDto {


    private Long id;

    private Long blendingAdviceDetailsId;
    private Long blendingAdviceStockId;

    private String casGroup;

    private String process;

    public List<BlendingPreProcessBinDetailsDto> binDetails;


    public BlendingAdvicePreProcessDto(BlendingAdvicePreProcess blendingAdvicePreProcess) {
        this.id = blendingAdvicePreProcess.getId();
        this.blendingAdviceDetailsId = blendingAdvicePreProcess.getBlendingAdviceDetailsId();
        this.blendingAdviceStockId = blendingAdvicePreProcess.getBlendingAdviceStockId();
        this.casGroup = blendingAdvicePreProcess.getCasGroup();
        this.process = blendingAdvicePreProcess.getProcess();

        this.binDetails = blendingAdvicePreProcess.getPreProcessBinDetails() != null
                ? blendingAdvicePreProcess.getPreProcessBinDetails().stream().map(field -> new BlendingPreProcessBinDetailsDto(field)).collect(Collectors.toList()) : null;

    }

}
