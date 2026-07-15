package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindmillDto {
	private Long id;
	private String name;
	private String type;
	private String description;
	private String status;
	private Double tDLoss;
	private Double mf;
	private Integer otherChargesPayableHtServiceId;
	private Double capacity;

}
