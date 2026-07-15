package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OilPackedDto {
	
	private Long sjid;
	private String name;
	public OilPackedDto(UnitReportMapList list) {
		
		this.sjid = list.getJournalId();
		this.name = list.getProductName1();
	}
	

}
