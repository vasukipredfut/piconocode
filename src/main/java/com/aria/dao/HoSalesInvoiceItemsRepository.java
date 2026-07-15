package com.aria.dao;

import com.aria.model.HoSalesInvoiceItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoSalesInvoiceItemsRepository extends JpaRepository<HoSalesInvoiceItems,Long> {


}
