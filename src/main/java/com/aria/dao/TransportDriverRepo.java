package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.aria.model.Machine;
import com.aria.model.TransportDriver;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportDriverRepo extends JpaRepository<TransportDriver,Long> {
	public List<TransportDriver> findByVehicleId(@Param("vehicleId") Machine vehicleId);
}
