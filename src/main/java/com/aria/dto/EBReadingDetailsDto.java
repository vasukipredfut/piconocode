package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EBReadingDetailsDto {
	
	private String attributename;
	
	private Long attributeid;
	
	private Integer orderid;
	
	private Double opening;
	
	private Double closing;

}
