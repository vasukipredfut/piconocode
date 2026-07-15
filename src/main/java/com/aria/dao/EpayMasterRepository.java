package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.EpayMaster;

public interface EpayMasterRepository extends JpaRepository<EpayMaster,Long>{

	public EpayMaster findByEmpcode(Long empcode);

	public Page<EpayMaster> findAll(Pageable pageable);

}
