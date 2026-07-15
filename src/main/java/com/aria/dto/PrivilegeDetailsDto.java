package com.aria.dto;

import com.aria.model.PrivilegeDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PrivilegeDetailsDto {

	private Long privilegeId;

	private String privilegeName;

	public PrivilegeDetailsDto(PrivilegeDetails model) {
		this.privilegeId = model.getPrivilegeId();
		this.privilegeName = model.getPrivilegeName();
	}

}
