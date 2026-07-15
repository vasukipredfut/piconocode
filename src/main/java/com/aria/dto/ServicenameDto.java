package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicenameDto {
	
	private Long id;
	
	private String name;

	public ServicenameDto(ServiceNameDtoProjection request) {
		super();
		this.id = request.getId();
		this.name = request.getName();
	}
	
	

}
