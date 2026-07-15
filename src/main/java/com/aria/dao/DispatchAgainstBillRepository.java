package com.aria.dao;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.DispatchAgainstBill;

@Repository
public interface DispatchAgainstBillRepository extends JpaRepository<DispatchAgainstBill,Long> {

}
