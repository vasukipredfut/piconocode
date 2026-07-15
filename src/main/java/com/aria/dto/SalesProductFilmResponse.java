
package com.aria.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesProductFilmResponse {

	private Long id;
	private Long productId;
	private String productName;
	private Long productGroupID;
	private String productGroupName;
	private Long capacityId;
	private String capacity;
	private Long branchID;
	private String branchName;
	private Long finalProductId;
	private String finalProductName;
	private LocalDate createdDate;
	private String altValue;
}
