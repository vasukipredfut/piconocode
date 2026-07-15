
package com.aria.dto;

import com.aria.model.Designation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DesignationDto {

	private Long id;

	private String designationName;

	public DesignationDto(Designation designation) {
		this.id = designation.getId();
		this.designationName = designation.getName();
	}
}
