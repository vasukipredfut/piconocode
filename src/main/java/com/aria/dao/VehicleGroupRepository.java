package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aria.model.VehicleGroup;
import com.aria.model.VehicleGroupingHead;

public interface VehicleGroupRepository extends JpaRepository<VehicleGroup,Long>{
	@Query(value="select * from vehicle_group where group_head =:groupHead",nativeQuery = true)
	public List<VehicleGroup> findByGroup_head(VehicleGroupingHead groupHead);
	
	@Query(value="select * from vehicle_group where vehicle_no =:vehicleNumber",nativeQuery = true)
	public VehicleGroup findByVehicleNo(String vehicleNumber);
}
