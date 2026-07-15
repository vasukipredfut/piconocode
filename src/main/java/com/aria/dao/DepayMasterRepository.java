package com.aria.dao;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.DepayMaster;
import com.aria.model.ListUser;

public interface DepayMasterRepository extends JpaRepository<DepayMaster,Long>{
	public DepayMaster findByCode(String empcode);
	public DepayMaster findByEmpcode(Long empcode);
	public Page<DepayMaster> findAll(Pageable pageable);
	
	@Query(value="SELECT distinct desi.name from designation desi inner join depaymaster d on d.desigid=desi.id",nativeQuery = true)
	public List<String> findAllByDesignation(Pageable page);
	
	@Query(value="SELECT distinct desi.name from designation desi inner join depaymaster d on d.desigid=desi.id where desi.name like :designation",nativeQuery = true)
	public List<String> findAllByDesignationByDesgnation(String designation,Pageable page);
	
	@Query(value = "SELECT u.id as id, u.name as employeeName, u.status as status, u.desigid as designationId, design.name"
			+ " as designationName,  u.bid as branchId, br.name as branchName, dpt.name as departmentName, u.did as "
			+ "departmentId FROM depaymaster u Left join branch br on br.id=u.bid left join "
			+ "department_details dpt on dpt.id=u.did left join designation design on design.id=u.desigid;",  nativeQuery = true)
	List<ListUser> wagesEmployeelistwfilter(@Param("branchId") Long branchId, @Param("departmentId") Long departmentId,
							  @Param("designationId") Long designationId);
}
