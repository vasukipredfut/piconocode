package com.aria.dao;

import com.aria.dto.OtherDeductionData;
import com.aria.dto.OtherDeductionList;
import com.aria.model.OtherDeduction;
import com.aria.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface OtherDeductionRepo extends JpaRepository<OtherDeduction, Long> {

	@Query(value = "select od.conid as empId, SUM(amount) as amt from otherdeduction od  WHERE od.conid=:userId AND od.contable =:contable"
			+ " AND (od.created_time BETWEEN :fromDate AND :toDate) " + " GROUP BY od.conid ", nativeQuery = true)
	OtherDeductionData findBy(@Param("userId") Long userId, @Param("contable") String contable,
			@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

	@Query(value = " select o.contable as connectTable ,o.id as id, o.conid as empId , ed.employeename as empName, o.name as name , o.amount as amount , o.status as status ,"
			+ "	o.description as description, org.name as companyName , org.id as companyId, o.branch as branchId, b.name as branchName ,"
			+ "	o.dept as deptId, dd.name as depName, o.month as month , o.year as year from otherdeduction o "
			+ "	left join organisation org on org.id = o.cid left join branch b on b.id = o.branch left join "
			+ "	department_details dd on dd.id = o.dept left join employee_details ed on ed.id = o.conid "
			+ "	WHERE (:company is null or org.id=:company) AND ( :contable is null or o.contable =:contable )"
			+ "	AND (:branch is null or o.branch=:branch ) AND ( :dept is null or o.dept =:dept ) and (o.generatebill =false)"
			+ "	AND (:month is null or o.month=:month) and (:year is null or o.year=:year) and (:name is null or o.name=:name)"
			+ "	ORDER BY o.id DESC ", nativeQuery = true)
	Page<OtherDeductionList> findByStaffList(@Param("company") Long company, @Param("branch") Long branch,
			@Param("dept") Long dept, @Param("contable") String contable, @Param("month") Long month,
			@Param("name") String type, @Param("year") Long year, Pageable pageable);

	@Query(value = " select o.contable as connectTable ,o.id as id, o.conid as empId ,  ed.name as empName ,  o.name as name , o.amount as amount , o.status as status , "
			+ " o.description as description, org.name as companyName , org.id as companyId, o.branch as branchId, b.name as branchName , "
			+ " o.dept as deptId, dd.name as depName, o.month as month , o.year as year from otherdeduction o "
			+ " left join organisation org on org.id = o.cid left join branch b on b.id = o.branch left join "
			+ " department_details dd on dd.id = o.dept left join emast_daily ed on ed.id = o.conid "
			+ " WHERE (:company is null or org.id=:company) AND ( :contable is null or o.contable =:contable )"
			+ " AND (:branch is null or o.branch=:branch ) AND ( :dept is null or o.dept =:dept ) and (o.generatebill =false) "
			+ " AND (:name is null or o.name=:name) and (:month is null or o.month=:month)"
			+ " AND (:year is null or o.year=:year) ORDER BY o.id DESC ", nativeQuery = true)
	Page<OtherDeductionList> findByWagesList(@Param("company") Long company, @Param("branch") Long branch,
			@Param("dept") Long dept, @Param("contable") String contable, @Param("month") Long month,
			@Param("name") String type, @Param("year") Long year, Pageable pageable);

	// (19-08-2025) changed by sivanantham(added three new filters(employeename,deductiontype,dacatid))
	// change suggested by alagu sir
	@Query(value = "select o.contable as connectTable ,o.id as id, o.conid as empId , ed.employeename as empName, o.name as name , o.amount as amount , o.status as status ,"
			+ " o.description as description, org.name as companyName , org.id as companyId, o.branch as branchId, b.name as branchName ,"
			+ " o.dept as deptId, dd.name as depName, o.month as month , o.year as year,o.generatebill as generatebill from otherdeduction o "
			+ "	left join organisation org on org.id = o.cid left join branch b on b.id = o.branch left join "
			+ "	department_details dd on dd.id = o.dept left join employee_details ed on ed.id = o.conid "
			+ "	WHERE  (ed.dacatid =:dacatid) AND ( :contable is null or o.contable =:contable )"
			+ "	and (:name is null or o.name=:name)AND (:branch is null or o.branch=:branch ) and (:company is null or org.id=:company) "
			+ "	AND (:month is null or o.month =:month) and (:year is null or o.year =:year) "
			+ "	and (:employeename is null or (lower(ed.employeename) LIKE ('%' || lower(:employeename) || '%')))"
			+ "	AND ( :dept is null or o.dept =:dept ) ORDER BY o.id desc", nativeQuery = true)
	Page<OtherDeductionList> findByStaff(@Param("dacatid") Long dacatid, @Param("branch") Long branch,
			@Param("company") Long company, @Param("dept") Long dept, @Param("employeename") String employeename,
			@Param("month") Long Month, @Param("year") Long year, @Param("name") String detype,
			@Param("contable") String contable, Pageable pageable);

	// (19-08-2025) changed by sivanantham(added three new filters(employeename,deductiontype,dacatid))
	// change suggested by alagu sir
	@Query(value = " select o.contable as connectTable ,o.id as id, o.conid as empId ,  ed.name as empName ,  o.name as name , o.amount as amount , o.status as status ,"
			+ "	o.description as description, org.name as companyName , org.id as companyId, o.branch as branchId, b.name as branchName ,"
			+ "	o.dept as deptId, dd.name as depName, o.month as month , o.year as year from otherdeduction o "
			+ "	left join organisation org on org.id = o.cid left join branch b on b.id = o.branch left join "
			+ "	department_details dd on dd.id = o.dept left join emast_daily ed on ed.id = o.conid "
			+ "	WHERE  ( :contable is null or o.contable =:contable )"
			+ "	AND (:branch is null or o.branch=:branch ) AND ( :dept is null or o.dept =:dept ) and (ed.dacatid =1)"
			+ "	and (:company is null or org.id=:company) and (:name is null or o.name=:name)"
			+ "	and (:employeename is null or (lower(ed.name) LIKE ('%' || lower(:employeename) || '%')))"
			+ "	AND (:month is null or o.month=:month) and (:year is null or o.year=:year) ORDER BY o.id desc", nativeQuery = true)
	Page<OtherDeductionList> findByWage(@Param("employeename") String employeename, @Param("name") String type,
			@Param("branch") Long branch, @Param("dept") Long dept, @Param("company") Long company,
			@Param("month") Long month, @Param("year") Long year, @Param("contable") String contable,
			Pageable pageable);

}
