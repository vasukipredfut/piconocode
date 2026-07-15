package com.aria.dao;

import com.aria.model.ActionConfiguration;
import com.aria.model.VehicleRequestAllotmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRequestAllotRepository extends JpaRepository<VehicleRequestAllotmentEntity,Long> {

    VehicleRequestAllotmentEntity findByReqId(Long reqId);

    Page<VehicleRequestAllotmentEntity> findByStatusOrderByIdDesc(String status,Pageable pageable);


    List<VehicleRequestAllotmentEntity> findByStatus(String status);
}
