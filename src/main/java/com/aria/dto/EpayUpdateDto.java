package com.aria.dto;

import jakarta.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EpayUpdateDto {
	@Column
	private Integer basic;
	
	@Column
	private Integer da;
	
	@Column
	private Integer hra;
	
	@Column
	private Integer tot;
	
	@Column
	private Integer tds;
	
	@Column
	private Integer tdeduction;
	
	@Column
	private Integer netsalary;
	
	@Column
	private Integer eesi;
	
	@Column
	private Integer epfbase;
	
	@Column
	private Integer epf1;
	
	@Column
	private Integer epf2;
	
	@Column
	private Long empcode;
}
