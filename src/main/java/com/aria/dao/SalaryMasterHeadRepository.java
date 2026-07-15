package com.aria.dao;

import com.aria.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryMasterHeadRepository extends JpaRepository<SalaryMasterHead, Long> {

	SalaryMasterHead findByMw(String mw);

	SalaryMasterHead findByDw(String dw);



	@Query(value = "SELECT * FROM salary_master_head WHERE epf_eligible='Y' and mw ='Y' ", nativeQuery = true)
	List<SalaryMasterHead> findAllByMwEPIEligible();

	@Query(value = "SELECT * FROM salary_master_head WHERE esi_eligible='Y' and mw ='Y'", nativeQuery = true)
	List<SalaryMasterHead> findAllByMwESIEligible();

	@Query(value = "SELECT * FROM salary_master_head WHERE epf_eligible='Y' and dw ='Y' ", nativeQuery = true)
	List<SalaryMasterHead> findAllByDwEPIEligible();
	@Query(value = "SELECT * FROM salary_master_head WHERE esi_eligible='Y' and dw ='Y' ", nativeQuery = true)
	List<SalaryMasterHead> findAllByDwESIEligible();


}
