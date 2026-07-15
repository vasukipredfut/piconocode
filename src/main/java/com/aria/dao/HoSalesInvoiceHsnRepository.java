package com.aria.dao;

import com.aria.model.HoSalesInvoiceHsn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoSalesInvoiceHsnRepository extends JpaRepository<HoSalesInvoiceHsn, Long> {


}
