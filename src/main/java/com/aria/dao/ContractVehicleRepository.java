package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import com.aria.model.ContractVehicle;

public interface ContractVehicleRepository extends JpaRepository<ContractVehicle, Long> {
	
	@Query(value = "SELECT * FROM dpatch_contract_vehicle", nativeQuery = true)
	List<ContractVehicle> filterContractVehicle();

}
