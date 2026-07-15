package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.VoucherDesc;
import com.aria.model.VoucherItems;

@Repository
public interface VoucherDescRepository extends JpaRepository<VoucherDesc, Long> {

	@Query(value="select * from vexpdes where bid = :id  limit 1" , nativeQuery=true)	
	VoucherDesc findByBid(Long id);
	

}
