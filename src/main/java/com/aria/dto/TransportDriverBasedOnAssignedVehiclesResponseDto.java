package com.aria.dto;

import java.util.List;

import com.aria.model.Machine;
import com.aria.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransportDriverBasedOnAssignedVehiclesResponseDto {
	private List<User> driverDetails;
	private Machine vehicleDetails;
	
}
