package com.aria.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindmillTimingResponseDto {

    private Long windmillId;
    private String windmillName;
    private Double mf;
    private Double tdLoss;
    private Double capacity;

    private LocalDate fromDate;
    private LocalDate toDate;
    private Long month;
    private Long year;
    private Long workingDays;

    private Double rkvaOpening;
    private Double rkvaClosing;
    
    private List<TimingReadingDto> timings; // C1–C6
    
    private Map<String, Double> otherCharges;
    
    private  List<WindmillotherchargesSaveTrn> charges;
    
    
}

