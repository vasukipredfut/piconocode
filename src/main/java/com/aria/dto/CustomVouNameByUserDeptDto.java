package com.aria.dto;

import org.apache.commons.text.StringEscapeUtils;

import com.aria.model.CustomVoucherName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomVouNameByUserDeptDto {
	
	private Long id;
	private String name;
	
	public CustomVouNameByUserDeptDto(CustomVouNameByUserDeptList customVouName )
	{
		this.id=customVouName.getId();
		this.name=StringEscapeUtils.unescapeHtml4(customVouName.getName());
	}

}
