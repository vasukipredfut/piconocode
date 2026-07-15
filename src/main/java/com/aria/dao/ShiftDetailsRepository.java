package com.aria.dao;

import com.aria.model.ShiftDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftDetailsRepository extends JpaRepository<ShiftDetails, Long> {

}
