package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.CustomVouDeptDto;
import com.aria.dto.CustomVouNameByUserDeptDto;
import com.aria.dto.CustomVouNameByUserDeptList;
import com.aria.dto.CustomVouNameDropDownDto;

import com.aria.dto.CustomVoucherList;
import com.aria.model.CustomVoucherName;

@Repository
public interface CustomVoucherNameRepository extends JpaRepository<CustomVoucherName,Long> {

	
	@Query(value="SELECT * FROM vouname  WHERE name = :name AND dept IN (:dept)" , nativeQuery = true)
	List<CustomVoucherName> findByOldVoucher(@Param("name")  String name,@Param("dept") List<Long> dept);
	
	@Query(value="SELECT cv.id AS id,cv.name AS name,cv.prefix AS prefix,vf.name AS vouFirmId,vf.isono AS isono, cv.recordstatus AS recordStatus,  STRING_AGG(DISTINCT COALESCE(d.name, dp.name), ',') AS departmentNames	"
			+ " FROM vouname cv LEFT JOIN customvou_deptmap map ON map.customvounameid = cv.id LEFT JOIN department_details d ON d.id = map.dept LEFT JOIN department_details dp ON dp.id = cv.dept LEFT JOIN vouformat vf ON vf.id=cv.voufirmid"  
			+ "	WHERE (:name is null or lower(cv.name) like('%'|| lower(:name) || '%'))"
			+ "      AND (:firmId is null or  cv.voufirmid = :firmId )"
			+ "      AND (:dept is null "
			+ "        or  cv.dept = :dept "
			+ "          or map.dept = :dept) GROUP BY cv.id, cv.name, cv.prefix,vf.name, vf.isono, cv.recordstatus ORDER BY cv.id DESC",nativeQuery = true)
	Page<CustomVoucherList> filterCustomVoucher(@Param("name") String name,@Param("dept") Long dept,@Param("firmId") Long firmId, Pageable pageable);

	 @Query(value = "SELECT DISTINCT cv.id  as id, cv.name as name   FROM vouname cv"
	 		+ " LEFT JOIN customvou_deptmap map ON map.customvounameid = cv.id"
	 		+ " WHERE ( cv.dept = :deptId OR map.dept = :deptId)"
	 		+ " ORDER BY cv.name ASC", nativeQuery = true)
		    List<CustomVouNameByUserDeptList> findDistinctVoucherNamesByDept(@Param("deptId") Long deptId);

 
	 @Query(value ="SELECT DISTINCT  cv.id as Id, cv.name as Name FROM vouname cv LEFT JOIN customvou_deptmap map ON map.customvounameid = cv.id JOIN expvoucher ev ON ev.vouid = cv.id WHERE (((cv.dept =:deptId  OR map.dept =:deptId) AND ev.dept =:deptId ) OR ((cv.dept = -1 OR map.dept = -1) AND ev.dept = -1 )) ORDER BY cv.name ASC ", nativeQuery = true)
	 List<CustomVouNameByUserDeptList> findByUserDeptId(@Param("deptId") Long deptId);

	 
	 @Query(value = "SELECT m.dept_id FROM machine m WHERE m.id=:id  ", nativeQuery = true)
	Long findDeptIdByMachineId(@Param("id") Long id);
	 @Query(value = "SELECT m.dept_id FROM employee_details m WHERE m.id=:id  ", nativeQuery = true)
	Long findDeptIdByStaffId(@Param("id")  Long conid);
	 @Query(value = "SELECT m.branch FROM machine m WHERE m.id = :id  ", nativeQuery = true)
		Long findBranchIdByMachineId(@Param("id") Long id);
		 @Query(value = "SELECT m.branch_id FROM employee_details m WHERE m.id=:id  ", nativeQuery = true)
		Long findBranchIdByStaffId(@Param("id")  Long conid);

	
		

		
	 
		
	
}