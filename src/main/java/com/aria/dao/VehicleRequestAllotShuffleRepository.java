package com.aria.dao;

import com.aria.model.VehicleRequestAllotmentDriverEntity;
import com.aria.model.VehicleRequestAllotmentShuffleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VehicleRequestAllotShuffleRepository extends JpaRepository<VehicleRequestAllotmentShuffleEntity,Long> {
    @Transactional
    @Modifying
    void deleteByAllotmentId(Long allotmentId);
    VehicleRequestAllotmentShuffleEntity findByAllotmentId(Long allotmentId);
}
