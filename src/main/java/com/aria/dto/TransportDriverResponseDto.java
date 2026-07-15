package com.aria.dto;

import com.aria.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransportDriverResponseDto {
	private Long driverId;
	private String driverName;
	
	public TransportDriverResponseDto(User user) {
		this.driverId=user.getId();
		this.driverName=user.getEmployeename();
	}
}
