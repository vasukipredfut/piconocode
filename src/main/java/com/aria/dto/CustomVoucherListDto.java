package com.aria.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomVoucherListDto {	
	
	private   Long id;
    private  String name;
    private  String prefix;
    private  String vouFirmId;
    private  List<String> departmentNames;
    private String recordStatus;
    private String isono;
    public CustomVoucherListDto(CustomVoucherList customVoucherList)
    {
    	this.id=customVoucherList.getId();
    	this.name=customVoucherList.getName();
    	this.prefix=customVoucherList.getPrefix();
    	this.vouFirmId=customVoucherList.getVouFirmId();    	
    	String deptStr = customVoucherList.getDepartmentNames();
        this.departmentNames = deptStr != null
                ? Arrays.asList(deptStr.split("\\s*,\\s*"))
                : new ArrayList<>();
        this.recordStatus=customVoucherList.getRecordStatus();
        this.isono=customVoucherList.getIsono();
    }


}
