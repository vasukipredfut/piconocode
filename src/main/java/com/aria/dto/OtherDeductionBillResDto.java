package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtherDeductionBillResDto {
	Long deductinId;
	Long empId;
	String empName;
	Double amount;
}
