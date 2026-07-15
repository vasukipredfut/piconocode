package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransportVehicleTypeDto {
	private String name;
	private String number;
	private Long id;
	
	public TransportVehicleTypeDto(String name,Long id,String number) {
		this.number=number;
		this.name=name;
		this.id=id;
	}
}
