package com.aria.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindmillOtherChargesTrnDto {
	
	private Long id;
    private Long windmillId;
    private Long windmillOtherChargesId;
    private LocalDate trnDate;
    private Double amount;
    private Long updatedBy;
    private Integer month;
    private Integer year;

}
