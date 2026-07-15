package com.aria.dto;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.aria.model.VoucherEntry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoucherEnrtyDto {

	private Long id;
	private Long vouId;
	private String vouType;
	private Long firmId;
	private String modeOfPay;
	private LocalDate vouDt;
	private String description;
	private String bankAcNo;
	private String bankCode;
	private String payableToName;
	private String ifscCode;
	private Double amt;
	private Long department;
	private Long branch;
	private String grpNo;
	private String vouName;
	
	
	List<VoucherItemsDto> items;

	
	
	
}
