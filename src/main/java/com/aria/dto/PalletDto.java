package com.aria.dto;

import com.aria.model.GogroupPalletNumberMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PalletDto {
	
	 private String palletType;
	private String palletNumber;
	 private Long binId;
	
	public PalletDto(GogroupPalletNumberMap gogroupPallet)
	{
		
		this.palletType=gogroupPallet.getPalletType();
		this.palletNumber=gogroupPallet.getPalletNumber();
		this.binId=gogroupPallet.getBinId();
	}

}
