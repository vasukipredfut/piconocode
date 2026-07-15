package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
public class OilPackedDataDto {
	     private Long id;
	    private String tableName;
	 private List<UnitReportPackingDto> packingList;
}
