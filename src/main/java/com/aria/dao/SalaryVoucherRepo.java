package com.aria.dao;

import com.aria.model.SalaryVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryVoucherRepo extends JpaRepository<SalaryVoucher,Long> {

}
