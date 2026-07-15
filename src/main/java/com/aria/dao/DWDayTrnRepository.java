package com.aria.dao;

import com.aria.model.DWDayTrn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DWDayTrnRepository extends JpaRepository<DWDayTrn, Long> {


}
