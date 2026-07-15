package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DayEndClosingReportDto {


    private Map<String, Object> inputProducts;
    private Map<String, Object> outputProducts;
    private Map<String, Object> summaryProductMap;
    private List<UnitReportClosingSummaryDto> summaryList;
   
   private List<OilPackedDataDto>packingData;
    private Company firmDetails;
    private Object plantDetails;


}
