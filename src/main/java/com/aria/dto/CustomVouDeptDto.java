package com.aria.dto;

import java.util.List;

import com.aria.model.CustomVouDeptMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomVouDeptDto {

	private Long dept ;
	
	
	public CustomVouDeptDto (CustomVouDeptMap customVouNameMap)
	{
		this.dept=customVouNameMap.getDept();
	}


	
}
