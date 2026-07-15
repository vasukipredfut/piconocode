package com.aria.dto;

import jakarta.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepayUpdateDto {
	@Column
	private Integer basic;
	
	@Column
	private Integer da;
	
	@Column
	private Integer allowance;
	
	@Column
	private Integer empcode;
	
	@Column
	private Integer epfbase;
	
	@Column
	private String tds;
}
