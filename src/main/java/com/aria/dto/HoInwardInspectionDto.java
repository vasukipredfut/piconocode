package com.aria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class HoInwardInspectionDto {
    @JsonProperty("LORRY_DATE")
    private String LORRY_DATE;  //27-09-2024
    @JsonProperty("LORRY_NO")
    private String LORRY_NO;  //S01
    @JsonProperty("BILL_NO")
    private String BILL_NO;  //2024-25/PODC-249
    @JsonProperty("BILL_DATE")
    private String BILL_DATE;  //26-09-2024
    @JsonProperty("BILL_ID")
    private Long BILL_ID;  //157509
    @JsonProperty("INSPECTION_TYPE")
    private String INSPECTION_TYPE;  // Fresh Report
    @JsonProperty("OIL_FFA")
    private String OIL_FFA;  //0.57
    @JsonProperty("TOTAL_OIL_OUTERN")
    private String TOTAL_OIL_OUTERN;  //32.25
    @JsonProperty("AVERAGE_WASTE")
    private String AVERAGE_WASTE;  //2.15
    @JsonProperty("OIL_TASTE_CATEGORY")
    private String OIL_TASTE_CATEGORY;  //Green
    @JsonProperty("OIL_COLOR_CATEGORY")
    private String OIL_COLOR_CATEGORY;  //Green
    @JsonProperty("COMMENT_ON_TASTE")
    private String COMMENT_ON_TASTE;  // லேஸ் தொண்டைபிடிப்பு, இலை துாசி மனம், கசப்பு, கமறல்,
    @JsonProperty("RECEIVED_QTY")
    private String RECEIVED_QTY;   // 51 Bags (1530 Kg)
    @JsonProperty("SCORE_OUTOFF_HUNDRED")
    private Long SCORE_OUTOFF_HUNDRED;   // 96
    @JsonProperty("PEROXIDE_VALUE")
    private String PEROXIDE_VALUE;   // 0.93
    @JsonProperty("IODINE_VALUE")
    private String IODINE_VALUE;   // 96.16
    @JsonProperty("MOISTURE")
    private String MOISTURE;   // 5.70
    @JsonProperty("TRANSFER_BY")
    private String TRANSFER_BY;   // TRANSFER EMP_NAME
    @JsonProperty("TRANSFER_DATE")
    private String TRANSFER_DATE;   // 30-09-2024

}


