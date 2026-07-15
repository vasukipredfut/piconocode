package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VUserBankDetailsDto {

	private Long id;
	private String name;
	private String bName;
	private String bAcNo;
	private String ifscCode;
	
	public VUserBankDetailsDto(VUserBankDetails vUserBankDetails)
	{		
		
		this.id = vUserBankDetails.getId();
		this.name = vUserBankDetails.getName();
		this.bName = vUserBankDetails.getBName();
		this.bAcNo = vUserBankDetails.getBAcNo();
		this.ifscCode = vUserBankDetails.getIfscCode();
	}
	
	
}
