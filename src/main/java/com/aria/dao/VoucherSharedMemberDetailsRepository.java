package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.VoucherSharedMemberDetails;

@Repository
public interface VoucherSharedMemberDetailsRepository extends JpaRepository<VoucherSharedMemberDetails, Long> {

	@Query(value="select * from vexgen where bid = :vouid And refid = :refid " , nativeQuery=true)
	List<VoucherSharedMemberDetails> findByBid(Long vouid , Long refid);

}
