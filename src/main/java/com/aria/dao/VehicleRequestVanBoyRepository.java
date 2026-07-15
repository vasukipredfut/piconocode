package com.aria.dao;

import com.aria.model.VehicleRequestRefTypeEntity;
import com.aria.model.VehicleRequestVanBoyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRequestVanBoyRepository extends JpaRepository<VehicleRequestVanBoyEntity, Long> {

    List<VehicleRequestVanBoyEntity> findByAllotmentId(Long allotmentId);

}
