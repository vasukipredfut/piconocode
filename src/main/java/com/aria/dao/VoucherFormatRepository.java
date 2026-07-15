package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.VoucherFormat;

@Repository
public interface VoucherFormatRepository extends JpaRepository<VoucherFormat, Long> {

	boolean existsByName(String name);
	

}
