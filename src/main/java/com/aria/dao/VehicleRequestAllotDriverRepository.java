package com.aria.dao;

import com.aria.model.VehicleRequestAllotmentDriverEntity;
import com.aria.model.VehicleRequestAllotmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VehicleRequestAllotDriverRepository extends JpaRepository<VehicleRequestAllotmentDriverEntity,Long> {
    @Transactional
    @Modifying
    void deleteByAllotmentId(Long allotmentId);
    List<VehicleRequestAllotmentDriverEntity> findByAllotmentId(Long allotmentId);
}
