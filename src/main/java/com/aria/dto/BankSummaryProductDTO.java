package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
public class BankSummaryProductDTO {
	 public String itemName;
//	    List<Double> lots ;
	 private List<LotWiseGodownDto> lots;
//	    List<GoDownDetailsForBankDto> goDownDetails;
}
