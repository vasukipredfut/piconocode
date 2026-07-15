package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinStockQuantityDto {

	private Long branch;
	private Long dept;
	private String branchName;
	private String deptName;
	private Double quantity;
	private String productName;
}
