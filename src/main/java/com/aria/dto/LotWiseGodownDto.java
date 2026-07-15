package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LotWiseGodownDto {
	 private Double lot;
	 private Long branch;
	 private String brName;
	    private List<GoDownDetailsForBankDto> godowns;

}
