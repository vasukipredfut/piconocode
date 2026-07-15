package com.aria.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.aria.model.CustomVoucherName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomVoucherNameDto {
	
	 private Long id;
	
	private String name;
	
	private String prefix;
	
	private Long vouFirmId;
	
	List<CustomVouDeptDto> deptList;
	
	private String recordStatus;
	
	public CustomVoucherNameDto(CustomVoucherName customVouName )
	{
		this.id=customVouName.getId();
		this.name=customVouName.getName();
		this.prefix=customVouName.getPrefix();
		this.vouFirmId=customVouName.getVouFirmId();
		
		if (customVouName.getDept() != null) {
	        CustomVouDeptDto deptDto = new CustomVouDeptDto();
	        deptDto.setDept(customVouName.getDept()); 
	        this.deptList = new ArrayList<>();
            this.deptList.add(deptDto);
	       
	    }
		
		this.recordStatus=customVouName.getRecordStatus();
	}
	

}
