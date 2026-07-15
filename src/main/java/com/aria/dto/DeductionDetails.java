package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeductionDetails {
     public Long deductionId;
     public String empName;
	 public Long empId;
	 public double amount;
}
