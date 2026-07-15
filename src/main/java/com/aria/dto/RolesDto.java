package com.aria.dto;

import com.aria.model.Roles;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolesDto {

	private Long id;

	private String name;

	public RolesDto(Roles model) {

		this.id = model.getId();
		this.name = model.getName();
	}
}
