package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class DpatchDeliveryRequestDto {
	private Long dispatchId;
	private String deliveryDate;
	private Long vehicleType;
	private Long partyName;
	private String vehicleNumber;
}
