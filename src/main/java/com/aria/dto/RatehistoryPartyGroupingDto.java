package com.aria.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RatehistoryPartyGroupingDto {

	private LocalDateTime date;

	private Long incDec;

	private Double amount;

	private Double oldAmount;
}
