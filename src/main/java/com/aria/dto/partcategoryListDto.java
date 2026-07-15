package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class partcategoryListDto {

	 private String trnNo;
	 private String dataResValues;
	 
	 public partcategoryListDto(partcategoryList pList)
	 {
		 this.trnNo=pList.getTrnNo();
		 this.dataResValues=pList.getDataResValues();
	 }
}
