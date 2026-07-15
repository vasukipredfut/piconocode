package com.aria.dao;

import com.aria.model.VehicleRequestRefTypeEntity;
import com.aria.model.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRequestRefTypeRepository extends JpaRepository<VehicleRequestRefTypeEntity, Long> {

    List<VehicleRequestRefTypeEntity> findByAllotmentId(Long allotmentId);

}
