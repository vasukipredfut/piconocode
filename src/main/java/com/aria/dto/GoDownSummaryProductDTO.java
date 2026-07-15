package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GoDownSummaryProductDTO {

    public String itemName;
    List<Double> lots ;
    List<GoDownSummaryProductMenuDTO> productMenuTitles;
}
