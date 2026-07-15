package com.aria.dao;

import com.aria.model.HoSalesOrderHsn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoSalesOrderHsnRepository extends JpaRepository<HoSalesOrderHsn, Long> {


}
