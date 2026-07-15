package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesvraReqDto {
     
	  public Long company;
	  public Long branch;
	  public String empType;
	  public String deductionType;
	  public Long month;
	  public Long year;
	  List<DeductionDetails> deductiondetails;
}
