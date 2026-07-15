package com.aria.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.AwardMaintainance;

@Repository
public interface AwardMaintainanceRepository extends JpaRepository<AwardMaintainance, Long> {


}