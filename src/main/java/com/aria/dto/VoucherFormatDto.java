package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.VoucherFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoucherFormatDto {
	
	private Long id;
	
	private String name;
	
	private String isono;
	public VoucherFormatDto(VoucherFormat voucherFormat)
	{
		this.id=voucherFormat.getId();
		this.name=voucherFormat.getName();
		this.isono=voucherFormat.getIsono();
		
	}
	

}
