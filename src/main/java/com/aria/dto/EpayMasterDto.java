package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EpayMasterDto {
	private String name;
	private Long empcode;
	private Integer tdeduction;
	private Integer netsalary;
	private Integer blsalary;
	private Integer flsalary;
	private Integer lsalary;
	private Integer alsalary;
	private double perdayamount; 
}
