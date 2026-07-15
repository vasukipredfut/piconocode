package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransportDto {
	private Long selectedVehicle;
	private String vehicleType;
	private String capacity;
	private String tankCapacity;
	private String fuelType;
	private Integer maximumMilage;
	private Long driverId;
}
