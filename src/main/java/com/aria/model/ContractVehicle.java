package com.aria.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.ContractVehicleResponseDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "dpatch_contract_vehicle")
public class ContractVehicle {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column (name = "dpatch_Contract_Vehicle_Id")
    private Long dpatch_Contract_Vehicle_Id;
	
	@Column (name = "cvehicle_Name")
	private String cvehicle_Name;
	
	@Column (name = "cvehicle_Number")
	private String cvehicle_Number;
	
	public ContractVehicle(ContractVehicleResponseDto contractVehicle) {
		this.dpatch_Contract_Vehicle_Id = contractVehicle.getDpatch_Contract_Vehicle_Id();
		this.cvehicle_Number = contractVehicle.getCvehicle_Number();
		this.cvehicle_Name = contractVehicle.getCvehicle_Name();
	}

}
