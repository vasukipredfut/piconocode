package com.aria.dto;

import com.aria.model.LabelGroup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LabelGroupDto {

	private Long id;

	private String name;

	private String code;

	public LabelGroupDto(LabelGroup model) {
		this.id = model.getId();
		this.name = model.getName();
		this.code = model.getCode();

	}

}
