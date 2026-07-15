package com.aria.dto;

import com.aria.model.Modules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModulesDto {

	private Long id;

	private String moduleName;

	public ModulesDto(Modules model) {
		this.id = model.getId();
		this.moduleName = model.getName();
	}

}
