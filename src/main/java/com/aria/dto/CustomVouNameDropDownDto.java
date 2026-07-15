package com.aria.dto;

import com.aria.model.CustomVoucherName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomVouNameDropDownDto {
	private Long id;
	private  String name;
	private String recordStatus;
	private Long FirmId;
	
	public CustomVouNameDropDownDto(CustomVoucherName customVoucherName)
	{
		this.id=customVoucherName.getId();
		this.name=customVoucherName.getName();
		this.FirmId=customVoucherName.getVouFirmId();
		this.recordStatus=customVoucherName.getRecordStatus();
	}
	

}
