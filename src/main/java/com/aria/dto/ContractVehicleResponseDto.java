package com.aria.dto;

import com.aria.model.ContractVehicle;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContractVehicleResponseDto {

	private Long dpatch_Contract_Vehicle_Id;

	private String cvehicle_Name;

	private String cvehicle_Number;

	public ContractVehicleResponseDto(ContractVehicle contractVehicle) {
		this.dpatch_Contract_Vehicle_Id = contractVehicle.getDpatch_Contract_Vehicle_Id();
		this.cvehicle_Number = contractVehicle.getCvehicle_Number();
		this.cvehicle_Name = contractVehicle.getCvehicle_Name();
	}

}
