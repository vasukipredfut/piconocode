package com.aria.dto;


import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SpecMappingDto {

	
	private Long specType;
	
	private Long specId;
	
	private Long proposalId;
	private Long vendorId;
	private String vendorStatus;


}
