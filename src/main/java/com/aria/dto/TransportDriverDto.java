package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransportDriverDto {
	private Long id;
	
	private Long vehicleId;
	
	private List<Long> driverId;
	
	private Integer status;
	
}
