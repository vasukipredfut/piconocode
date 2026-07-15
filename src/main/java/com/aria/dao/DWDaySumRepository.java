package com.aria.dao;

import com.aria.model.DWDaySum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DWDaySumRepository extends JpaRepository<DWDaySum, Long> {


}
