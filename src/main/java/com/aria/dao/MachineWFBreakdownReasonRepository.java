package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.*;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MachineWFBreakdownReasonRepository extends JpaRepository<MachineWFBreakdownReason, Long> {
	@Query(value = "SELECT cm FROM MachineWFBreakdownReason cm ")
//			+ "WHERE (:name is null or (lower(cm.machine.name) LIKE ('%' || lower(:name) || '%'))) "
//			+ " AND (:id is null or(lower(cm.machine.id) LIKE ('%' || lower(:id) || '%')))")
	Page<MachineWFBreakdownReason> machineWFBreakdownList(Pageable pageable);
	@Transactional
	@Modifying
	void deleteByMachineWorkFlowId(Long machineWorkFlowId);


	List<MachineWFBreakdownReason> findByMachineWorkFlowId(Long machineWorkFlowId);



}