package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.VehicleGroup;
import com.aria.model.VehicleGroupingHead;

public interface VehicleGroupingHeadRepository extends JpaRepository<VehicleGroupingHead,Long>{
	@Query(value="select * from vehicle_grouping_head where type =:vehicleType and active_flag =:active",nativeQuery = true)
	public List<VehicleGroupingHead> findByType(Long vehicleType,Integer active);
	
	public List<VehicleGroupingHead> findByType(@Param("type") Long type);
}
