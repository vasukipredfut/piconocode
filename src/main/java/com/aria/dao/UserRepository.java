package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.aria.dto.CustomVouNameByUserDeptList;
import com.aria.dto.OtherDeductionList;
import com.aria.dto.OtherDeductionResList;
import com.aria.dto.UserList;
import com.aria.dto.VUserBankDetails;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aria.model.ListUser;
import com.aria.model.User;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query(value = "SELECT *  FROM employee_details u WHERE u.username =:username and (u.status='' OR u.status='A') ", nativeQuery = true)
	Optional<User> findByUsername(@Param("username") String username);

	@Query(value = "SELECT *  FROM employee_details u WHERE u.aadharno =:aadharno", nativeQuery = true)
	Optional<User> findByAadharno(@Param("aadharno") String aadharno);

	Boolean existsByUsername(String username);

	@Query(value = "SELECT * FROM employee_details WHERE (active IS NULL OR active IS NOT NULL) AND (final IS NULL OR final NOT IN ('Y'))", nativeQuery = true)
	Page<User> findAll(Pageable pagination);

	@Query(value = "SELECT * FROM employee_details u WHERE  "
			+ "(:employeeName is null or (lower(u.employeename) LIKE ('%' || lower(:employeeName) || '%')))", nativeQuery = true)
	List<User> findAllByEmployeeName(String employeeName);

	@Query(value = "SELECT * FROM employee_details u WHERE  " + " u.dept_id = :departmentId AND u.dacatid IS NOT NULL "
			+ " AND  u.branch_id = :branchId AND (u.status='' OR u.status='A')", nativeQuery = true)
	List<User> findAllByBranchAndDepartment(Long branchId, Long departmentId);

	@Query(value = "SELECT * FROM employee_details u WHERE  " + " u.dept_id = :departmentId "
			+ " AND u.dacatid IS NOT NULL AND  u.branch_id = :branchId "
			+ " AND  u.roles_id = :roleId  AND (u.status='' OR u.status='A') ", nativeQuery = true)
	List<User> findAllByBranchAndDepartmentAndRole(Long branchId, Long departmentId, Long roleId);

	@Query(value = "SELECT * FROM employee_details u WHERE  " + " u.dept_id = :departmentId "
			+ " AND u.dacatid IS NOT NULL AND  u.branch_id = :branchId "
			+ " AND  u.roles_id IN (:roleId)  AND (u.status='' OR u.status='A') ", nativeQuery = true)
	List<User> findAllByBranchAndDepartmentAndRoleIds(Long branchId, Long departmentId, List<Long> roleId);

	@Query(value = "SELECT * FROM employee_details u WHERE  " + " u.branch_id = :branchId AND u.dacatid IS NOT NULL "
			+ " AND  u.roles_id = :roleId AND (u.status='' OR u.status='A') ", nativeQuery = true)
	List<User> findAllByBranchAndRole(Long branchId, Long roleId);

	@Query(value = "SELECT distinct designation FROM employee_details", nativeQuery = true)
	List<String> findAllByDesignation();

	@Query(value = "SELECT *  FROM employee_details e WHERE e.id in (:userIds)", nativeQuery = true)
	List<User> findAllByUserId(@Param("userIds") List<Long> userIds);

	@Query(value = "SELECT u.id as id, u.employeename as employeeName, u.joiningdate as joiningDate, u.desi_id as designationId,"
			+ " u.status as status, d.name as designationName, u.branch_id as branchId, br.name as branchName, dpt.name as departmentName, "
			+ " u.dept_id as departmentId FROM employee_details u  LEFT JOIN designation d ON d.id = u.desi_id LEFT JOIN "
			+ " branch br ON br.id = u.branch_id LEFT JOIN department_details dpt ON dpt.id = u.dept_id   "
			+ " WHERE  (:branchId is null or u.branch_id = :branchId) "
			+ " AND (:employeeName is null or (lower(u.employeename) LIKE ('%' || lower(:employeeName) || '%')))"
			+ " AND (:employeeId is null or u.id = :employeeId)"
			+ " AND (:employeeType is null or u.employeetype = :employeeType)"
			+ " AND (:departmentId is null or u.dept_id = :departmentId)"
			+ " AND (:designationId is null or d.id = :designationId)"
			+ " AND (:designation is null or (lower(d.name) LIKE ('%' || lower(:designation) || '%')))"
			+ " AND (:status is null or (lower(u.status) LIKE ('%' || lower(:status) || '%')))"
			+ " AND (cast(:startDate as date) is null or (u.joiningdate >= :startDate))"
			+ " AND (cast(:endDate as date) is null or (u.joiningdate <= :endDate)) order by id desc", nativeQuery = true)
	Page<ListUser> filterListUserData(@Param("employeeId") Long employeeId, @Param("branchId") Long branchId,
			@Param("departmentId") Long departmentId, @Param("employeeName") String employeeName,
			@Param("employeeType") String employeeType, @Param("designationId") Long designationId,
			@Param("designation") String designation, @Param("status") String status,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

	@Query(value = "SELECT u.id as id, u.employeename as employeeName, u.status as status, u.desi_id as designationId, "
			+ " u.branch_id as branchId, br.name as branchName, dpt.name as departmentName, u.dept_id as departmentId "
			+ " FROM employee_details u, shift_assign sh, instrulog il, branch br, department_details dpt "
			+ " WHERE br.id=u.branch_id and dpt.id=u.dept_id and u.bioid=il.enrollnumber AND sh.emp_id=u.id "
			+ " AND (:branchId is null or u.branch_id = :branchId)"
			+ " AND (:employeeName is null or (lower(u.employeename) LIKE ('%' || lower(:employeeName) || '%')))"
			+ " AND (:employeeId is null or u.id = :employeeId)"
			+ " AND (:employeeType is null or u.employeetype = :employeeType)"
			+ " AND (:departmentId is null or u.dept_id = :departmentId)"
			+ " AND (:designationId is null or u.desi_id = :designationId)"
			+ " AND (:active is null or u.active = :active)" + " AND (:shiftid is null or sh.shift_id = :shiftid)"
			+ " AND (cast(:shiftDate as date) is null or (sh.from_date <= :shiftDate AND sh.to_date >= :shiftDate AND il.attdatetime >= :shiftDate)"
			+ " AND (:gender is null or u.gender = :gender))", nativeQuery = true)
	List<ListUser> stafflistwfilter(@Param("employeeId") Long employeeId, @Param("branchId") Long branchId,
			@Param("departmentId") Long departmentId, @Param("employeeName") String employeeName,
			@Param("employeeType") String employeeType, @Param("designationId") Long designationId,
			@Param("active") Integer active, @Param("shiftid") Long shiftid, @Param("gender") String gender,
			@Param("shiftDate") Date shiftDate);

	@Query(value = "SELECT u.id as id, u.employeename as employeeName, u.status as status, u.desi_id as designationId, "
			+ " design.name as designationName,  u.branch_id as branchId, br.name as branchName, dpt.name as departmentName, "
			+ " u.dept_id as departmentId, w.weekoffday as weekOffDay FROM employee_details u LEFT JOIN branch br ON br.id=u.branch_id "
			+ " LEFT JOIN department_details dpt ON dpt.id=u.dept_id  LEFT JOIN weekoff w ON w.emp_id=u.id  "
			+ " LEFT JOIN designation design ON design.id=u.desi_id  WHERE (:branchId is null or u.branch_id = :branchId)"
			+ " AND (:employeeName is null or (lower(u.employeename) LIKE ('%' || lower(:employeeName) || '%')))"
			+ " AND (:employeeId is null or u.id = :employeeId)"
			+ " AND (:employeeType is null or u.employeetype = :employeeType)"
			+ " AND (:departmentId is null or u.dept_id = :departmentId)"
			+ " AND (:location is null or u.location = :location)"
			+ " AND (:designationId is null or u.desi_id = :designationId) ORDER BY u.id DESC", nativeQuery = true)
	List<ListUser> stafflistfilter(@Param("employeeId") Long employeeId, @Param("branchId") Long branchId,
			@Param("departmentId") Long departmentId, @Param("employeeName") String employeeName,
			@Param("employeeType") String employeeType, @Param("designationId") Long designationId,
			@Param("location") String location);

	@Query(value = "SELECT distinct designation FROM employee_details WHERE designation LIKE :designation", nativeQuery = true)
	Page<String> findAllByDesignation(@Param("designation") String designation, Pageable pageable);

	@Query(value = "SELECT distinct category FROM employee_details", nativeQuery = true)
	List<String> findAllByCategory();

	@Query(value = "SELECT distinct status FROM interview", nativeQuery = true)
	List<String> findAllByStatus();

	@Query(value = "SELECT distinct status FROM interview WHERE status LIKE :status", nativeQuery = true)
	List<String> findAllByStatus(@Param("status") String status);

	@Query(value = "SELECT distinct qualification FROM employee_details WHERE (:qualification is null or (lower(qualification) LIKE ('%' || lower(:qualification) || '%')))", nativeQuery = true)
	List<String> findAllByQualification(@Param("qualification") String qualification);

	@Query(value = "SELECT distinct qualification FROM employee_details", nativeQuery = true)
	List<String> findAllByQualification();

	@Query(value = "SELECT *  FROM employee_details u WHERE u.bioid =:bioid ", nativeQuery = true)
	Optional<User> findByBioId(@Param("bioid") Long bioid);

    @Query(value = "SELECT *  FROM employee_details u WHERE u.bioid =:bioid and (u.status='' OR u.status='A') ", nativeQuery = true)
    Optional<User> findByBioIdWithActive(@Param("bioid") Long bioid);

    @Query(value = "SELECT * FROM employee_details u, shift_assign sh, instrulog il, branch br, department_details dpt WHERE br.id=u.branch_id and dpt.id=u.dept_id and u.bioid=il.enrollnumber AND sh.emp_id=u.id AND (:branchId is null or u.branch_id = :branchId)"
			+ "AND (:employeeId is null or u.id = :employeeId)"
			+ "AND (:departmentId is null or u.dept_id = :departmentId)"
			+ "AND (cast(:shiftDate as date) is null or (sh.from_date <= :shiftDate AND sh.to_date >= :shiftDate AND il.attdatetime >= :shiftDate))"
			+ "LIMIT 1", nativeQuery = true)
	Optional<User> checkAttendance(@Param("employeeId") Long employeeId, @Param("branchId") Long branchId,
			@Param("departmentId") Long departmentId, @Param("shiftDate") Date shiftDate);

	@Query(value = "SELECT * FROM employee_details u WHERE  (u.status='' OR u.status='A')  AND (u.id=:employeeId)", nativeQuery = true)
	Page<User> findById(@Param("employeeId") Long employeeId, Pageable pagination);

	@Query(value = "SELECT ed.id FROM User ed WHERE (ed.branch_id = :branchId AND (COALESCE(:userLevelSet) IS NULL or (ed.userlevel IN :userLevelSet)) AND (COALESCE(:categorySet) IS NULL or (ed.employeetype IN :categorySet)))")
	Set<Long> findByUserLevelInANDEmployeeCategoryIn(@Param("branchId") Long branchId,
			@Param("userLevelSet") Set<String> userLevelSet, @Param("categorySet") Set<String> categorySet);

	@Query(value = "SELECT * FROM employee_details u WHERE  u.employeename =:candidateName and u.phoneno =:phoneNum and u.aadharno=:aadharNumber", nativeQuery = true)
	Optional<User> findBy(@Param("aadharNumber") String aadharNumber, @Param("phoneNum") String phoneNum,
			@Param("candidateName") String candidateName);

	@Query(value = "SELECT u.id as id, u.employeename as employeeName, design.id as designationId, design.name as designationName, "
			+ " u.employeetype as EmployeeType , br.id as branchId, br.name as branchName, dpt.name as departmentName, "
			+ " dpt.id as departmentId FROM employee_details u LEFT JOIN branch br ON br.id=u.branch_id "
			+ " LEFT JOIN department_details dpt ON dpt.id=u.dept_id  LEFT JOIN designation design ON design.id=u.desi_id  "
			+ " WHERE (u.status='' OR u.status='A') AND (:branchId is null or br.id = :branchId) AND ( u.dacatid IS NOT NULL ) "
			+ " AND (:employeeName is null or (lower(u.employeename) LIKE ('%' || lower(:employeeName) || '%')))"
			+ " AND (:employeeId is null or u.id = :employeeId)" + " AND (:dacatId is null or u.dacatid = :dacatId)"
			+ " AND (:employeeType is null or u.employeetype = :employeeType)"
			+ " AND (:departmentId is null or u.dept_id = :departmentId)"
			+ " AND (:designationId is null or u.desi_id = :designationId) ORDER BY u.id DESC", nativeQuery = true)
	List<UserList> commonStaffFilter(@Param("employeeId") Long employeeId, @Param("dacatId") Long dacatId,
			@Param("branchId") Long branchId, @Param("departmentId") Long departmentId,
			@Param("employeeName") String employeeName, @Param("employeeType") String employeeType,
			@Param("designationId") Long designationId);

	List<User> findByDacatid(@Param("dacatId") Long dacatId);

	@Query(value = "SELECT * FROM employee_details WHERE LOWER(employeename) LIKE LOWER(CONCAT('%', ?1, '%')) AND dacatid = 3", nativeQuery = true)
	List<User> findbByName(@Param("driverName") String driverName);

	@Query(value = "Select * from employee_details where dacatid=3", nativeQuery = true)
	List<User> findbByName(Pageable page);

	@Query(value = "SELECT  * FROM employee_details e WHERE (e.status='' OR e.status='A') ", nativeQuery = true)
	List<User> findAlls();

	@Query(value = "SELECT *  FROM employee_details u WHERE u.email =:username and (u.status='' OR u.status='A') limit 1", nativeQuery = true)
	Optional<User> findByEmail(@Param("username") String username);

	@Query(value = "SELECT  * FROM employee_details e WHERE (e.status='' OR e.status='A') AND e.dacatid=1  AND e.org_id =:company AND e.branch_id =:branch order by id asc ", nativeQuery = true)
	List<User> findAllStaffs(@Param("company") Long company, @Param("branch") Long branch);

	@Query(value = "SELECT  * FROM employee_details e WHERE (e.status='' OR e.status='A') AND e.dacatid=6  AND e.org_id =:company AND e.branch_id =:branch order by id asc ", nativeQuery = true)
	List<User> findAllStaffApprentice(@Param("company") Long company, @Param("branch") Long branch);

	@Query(value = "SELECT  * FROM employee_details e WHERE e.id in (1,2,12,13,16,18,22) AND (e.status='' OR e.status='A') AND e.dacatid=1  AND e.org_id =:company AND e.branch_id =:branch order by id asc ", nativeQuery = true)
	List<User> findAllStaffsTesting(@Param("company") Long company, @Param("branch") Long branch);

	@Query(value = "SELECT *  FROM employee_details u WHERE u.id =:id AND u.email =:username AND u.status='L'  ", nativeQuery = true)
	Optional<User> findByEmailCheck(@Param("id") Long id, @Param("username") String username);

	@Query(value = "SELECT *  FROM employee_details u WHERE u.id =:id and (u.status='' OR u.status='A') limit 1", nativeQuery = true)
	Optional<User> findByIdValue(@Param("id") Long id);

	@Query(value = "select * from employee_details u WHERE u.employeetype =:employeetype", nativeQuery = true)
	List<User> findByEmployeeType(@Param("employeetype") String employeetype);

	// filter method for filter employee based on deduction
	@Query(value = " select u.id as empId,u.employeename  as employeeName from employee_details u "
			+ "	where (u.status='' OR u.status='A') and (:branch is null or u.branch_id =:branch) and (:dept is null or u.dept_id=:dept)"
			+ " and (u.dacatid =:dacatid) and  u.id not in (select o.conid from otherdeduction o "
			+ " where (:name is null or o.name=:name) and (:month is null or o.month=:month)"
			+ " and (:year is null or o.year=:year) and (:branch is null or o.branch=:branch)"
			+ " and (:dept is null or o.dept=:dept ) ) ", nativeQuery = true)
	Page<OtherDeductionResList> findByDeductionFillerForStaff(@Param("branch") Long branch, @Param("dept") Long dept,
			@Param("name") String type, @Param("month") Long month, @Param("year") Long year,@Param("dacatid")Long dacatid, Pageable pageable);

	@Query(value= " SELECT e.id as id , e.employeename as name , e.accountno as bAcNo , e.bankname as bName , e.ifsccode as ifscCode from  employee_details e  where e.id=:id " ,  nativeQuery=true)
	Optional<VUserBankDetails> findByBankDetails(@Param("id") Long id);
	

	 @Query(value = "SELECT e.id as id , e.employeename as name FROM employee_details e WHERE e.status = 'A' "
	 		+ "      AND ((:deptId IS NOT NULL AND e.dept_id = :deptId) OR (:branchId IS NOT NULL AND e.branch_id = :branchId) "
	 		+ " OR (:deptId IS NULL AND :branchId IS NULL))", nativeQuery = true)
	 List<CustomVouNameByUserDeptList> findActiveEmployees(@Param("deptId") Long deptId, @Param("branchId") Long branchId);
	 @Query(value = "SELECT u.id as id, u.employeename as employeeName, design.id as designationId, design.name as designationName, "
				+ " u.employeetype as EmployeeType , br.id as branchId, br.name as branchName, dpt.name as departmentName, "
				+ " dpt.id as departmentId FROM employee_details u LEFT JOIN branch br ON br.id=u.branch_id "
				+ " LEFT JOIN department_details dpt ON dpt.id=u.dept_id  LEFT JOIN designation design ON design.id=u.desi_id  "
				+ " WHERE (u.status='' OR u.status='A') AND (:branchId is null or br.id = :branchId) AND ( u.dacatid IS NOT NULL ) "
				+ " AND (:employeeName is null or (lower(u.employeename) LIKE ('%' || lower(:employeeName) || '%')))"
				+ " AND (:employeeId is null or u.id = :employeeId)" + " AND (:dacatId is null or u.dacatid != 6 )"
				+ " AND (:employeeType is null or u.employeetype = :employeeType)"
				+ " AND (:departmentId is null or u.dept_id = :departmentId)"
				+ " AND (:designationId is null or u.desi_id = :designationId) ORDER BY u.id DESC", nativeQuery = true)
		List<UserList> commonStaffFilterList(@Param("employeeId") Long employeeId, @Param("dacatId") Long dacatId,
				@Param("branchId") Long branchId, @Param("departmentId") Long departmentId,
				@Param("employeeName") String employeeName, @Param("employeeType") String employeeType,
				@Param("designationId") Long designationId);

	boolean existsByBioid(Long bioId);

	boolean existsByBioidAndIdNot(Long bioId, Long id);
}