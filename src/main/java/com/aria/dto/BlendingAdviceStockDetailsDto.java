package com.aria.dto;


import com.aria.model.BlendingAdviceStockDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class BlendingAdviceStockDetailsDto {


    private Long id;

    private Long blendingAdviceDetailsId;

    private Long productId;

    private String casGroup;

    private Double noOfBags;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;


    public List<BlendingAdviceBinDetailsDto> binDetails;

    public BlendingAdviceStockDetailsDto(BlendingAdviceStockDetails blendingAdviceStockDetails) {

        this.id = blendingAdviceStockDetails.getId();
        this.blendingAdviceDetailsId = blendingAdviceStockDetails.getBlendingAdviceDetailsId();
        this.casGroup = blendingAdviceStockDetails.getCasGroup();
        
        this.noOfBags = blendingAdviceStockDetails.getNoOfBags();

        this.created_date = blendingAdviceStockDetails.getCreated_date();
        this.updated_date = blendingAdviceStockDetails.getUpdated_date();
        this.productId = blendingAdviceStockDetails.getProductId();

        this.binDetails = blendingAdviceStockDetails.getBinDetails() != null
                ? blendingAdviceStockDetails.getBinDetails().stream().map(field -> new BlendingAdviceBinDetailsDto(field)).collect(Collectors.toList()) : null;

    }
}
