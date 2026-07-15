package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.dto.GroupNameList;
import com.aria.dto.VoucherItemsPrint;
import com.aria.model.VoucherItems;
@Repository
public interface VoucherItemsRepository extends JpaRepository<VoucherItems, Long> {

	@Query(value="select * from vouitems where vouid = :vouid " , nativeQuery=true)
	Optional<VoucherItems> findByVouId(Long vouid);

	@Query(value="select * from vouitems where vouid = :id " , nativeQuery=true)
	List<VoucherItems> findByVouid(Long id);

	
	@Query(value="select v.rcost AS unitRate , v.ltotal AS lineTotal , v.qty AS quantity , v.secexpname AS secName, v.refid AS refid from vouitems  v   where v.vouid = :id  " , nativeQuery=true)
	List<VoucherItemsPrint> findExpenseByVouid(Long id);

	@Query(value=" SELECT n.id,n.name,COALESCE(SUM(vi.ltotal), 0) AS totalCost FROM texpgname n LEFT JOIN texpgrp t ON t.grpid = n.id "
    + " LEFT JOIN vouitems vi ON vi.refid = t.expid AND vi.vouid IN( :vouIdList) GROUP BY n.id, n.name ORDER BY n.id ASC " , nativeQuery=true)
	List<GroupNameList> findByAmount(List<Long> vouIdList);

}
