package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.dto.CustomVouNameByUserDeptList;
import com.aria.dto.OtherDeductionResList;
import com.aria.dto.UserList;
import com.aria.model.ListUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.EmastDaily;

@Repository
public interface EmastDailyRepository extends JpaRepository<EmastDaily, Long> {

    public Optional<EmastDaily> findByPhoneno(String phoneno);

    public Page<EmastDaily> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM emast_daily u WHERE  (u.status='' OR u.status='A' OR u.status='0') AND ( u.dacatid IS NOT NULL ) "
            + " AND (:name is null or (lower(u.name) LIKE ('%' || lower(:name) || '%'))) ", nativeQuery = true)
    List<EmastDaily> findByName(String name);

    @Query(value = "select ed.id as id, ed.name as employeeName, ed.status as status , br.id as branchId, br.name as branchName, ed.joiningdate as joiningDate , "
            + " dt.id as departmentId , dt.name as departmentName , ed.desigid as designationId , ed.designation as designationName "
            + " FROM emast_daily ed LEFT JOIN branch br ON br.id = ed.branch LEFT JOIN department_details dt ON dt.id = ed.department "
            + " WHERE  (:branchId is null or ed.branch = :branchId)"
            + " AND (:employeeName is null or (lower(ed.name) LIKE ('%' || lower(:employeeName) || '%'))) "
            + " AND (:employeeId is null or ed.id = :employeeId)"
            + " AND (:departmentId is null or ed.department = :departmentId) "
            + " AND (:status is null or (lower(ed.status) LIKE ('%' || lower(:status) || '%'))) "
            + " AND (:designationId is null or ed.desigid = :designationId)"
            + " AND (:designation is null or (lower(ed.designation) LIKE ('%' || lower(:designation) || '%')))"
            + " AND (cast(:startDate as date) is null or (ed.joiningdate >= :startDate))"
            + " AND (cast(:endDate as date) is null or (ed.joiningdate <= :endDate)) "
            + " order by ed.id desc", nativeQuery = true)
    Page<ListUser> emastFilterList(@Param("employeeId") Long employeeId, @Param("branchId") Long branchId,
                                   @Param("departmentId") Long departmentId, @Param("employeeName") String employeeName,
                                   @Param("status") String status, @Param("designationId") Long designationId,
                                   @Param("designation") String designation, @Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate, Pageable pageable);

    @Query(value = "SELECT u.id as id, u.name as employeeName,  u.category as EmployeeType , br.id as branchId, br.name as branchName, "
            + " u.desigid as designationId , u.designation as designationName, dpt.name as departmentName, dpt.id as departmentId "
            + " FROM emast_daily u LEFT JOIN branch br ON br.id=u.branch "
            + " LEFT JOIN department_details dpt ON dpt.id=u.department  WHERE (:branchId is null or br.id = :branchId) "
            + " AND (:employeeName is null or (lower(u.name) LIKE ('%' || lower(:employeeName) || '%'))) "
            + " AND (:employeeId is null or u.id = :employeeId) "
            + " AND (:employeeType is null or u.category = :employeeType) "
            + " AND (:departmentId is null or u.department = :departmentId) "
            + " AND (u.status='' OR u.status='A' OR u.status='0') "
            + " AND (:dataCatId is null or u.dacatid = :dataCatId) AND ( u.dacatid IS NOT NULL ) "
            + " ORDER BY u.id DESC", nativeQuery = true)
    List<UserList> commonEmastStaffFilter(@Param("employeeId") Long employeeId, @Param("branchId") Long branchId,
                                          @Param("departmentId") Long departmentId, @Param("employeeName") String employeeName,
                                          @Param("employeeType") String employeeType, @Param("dataCatId") Long dataCatId);

    @Query(value = "SELECT u.id as id, u.name as employeeName,  u.category as EmployeeType , br.id as branchId, br.name as branchName, "
            + " u.desigid as designationId , u.designation as designationName, dpt.name as departmentName, dpt.id as departmentId "
            + " FROM emast_daily u LEFT JOIN branch br ON br.id=u.branch "
            + " LEFT JOIN department_details dpt ON dpt.id=u.department  WHERE (:branchId is null or br.id = :branchId) "
            + " AND (:employeeName is null or (lower(u.name) LIKE ('%' || lower(:employeeName) || '%'))) "
            + " AND (:employeeId is null or u.id = :employeeId) "
            + " AND (:employeeType is null or u.category = :employeeType) "
            + " AND (:departmentId is null or u.department = :departmentId) "
            + " AND (u.status='' OR u.status='A' OR u.status='0') "
            + " AND (u.dacatid <> 6)  AND ( u.dacatid IS NOT NULL ) " + " ORDER BY u.id DESC", nativeQuery = true)
    List<UserList> commonEmastStaffFilterByDaCat(@Param("employeeId") Long employeeId, @Param("branchId") Long branchId,
                                                 @Param("departmentId") Long departmentId, @Param("employeeName") String employeeName,
                                                 @Param("employeeType") String employeeType);

    @Query(value = "SELECT * FROM emast_daily u WHERE  u.name =:candidateName and u.phoneno =:phoneNum and u.adharno=:aadharNumber LIMIT 1", nativeQuery = true)
    Optional<EmastDaily> findBy(@Param("aadharNumber") String aadharNumber, @Param("phoneNum") String phoneNum,
                                @Param("candidateName") String candidateName);

    @Query(value = "SELECT  * FROM emast_daily e WHERE (e.status='' OR e.status='A' OR e.status='0') ", nativeQuery = true)
    List<EmastDaily> findAlls();

    @Query(value = "SELECT distinct qualification FROM emast_daily WHERE qualification IS NOT NULL and (:qualification is null or (lower(qualification) LIKE ('%' || lower(:qualification) || '%')))", nativeQuery = true)
    List<String> findAllByQualification(@Param("qualification") String qualification);

    @Query(value = "select u.*,u.name as employeeName from emast_daily u where  u.branch=:branch and u.department=:dept"
            + " and not exists(select * from otherdeduction o where o.conid=u.id and LOWER(o.name) =:name and o.month =:month and o.year =:year ) order by u.id desc", nativeQuery = true)
    List<EmastDaily> findByDeductionWage(@Param("branch") Long branch, @Param("dept") Long dept, @Param("name") String type,
                                         @Param("month") Long month, @Param("year") Long year);

    //created by sivanantham(create new filter method for fetch employees based on this filters)
    @Query(value = "select u.id as empId,u.name as employeeName from emast_daily u "
            + "	where (u.status='' OR u.status='A' OR u.status='0') and (u.dacatid = 1) and (:branch is null or u.branch=:branch) and (:dept is null or u.department=:dept)"
            + "  and  u.id not in (select o.conid from otherdeduction o "
            + " where (:name is null or o.name=:name) and (:month is null or o.month=:month)"
            + " and (:year is null or o.year=:year) and (:branch is null or o.branch=:branch)"
            + " and (:dept is null or o.dept=:dept )) order by u.id desc", nativeQuery = true)
    Page<OtherDeductionResList> findByDeductionWages(@Param("branch") Long branch, @Param("dept") Long dept, @Param("name") String type,
                                                     @Param("month") Long month, @Param("year") Long year, Pageable pageable);

    @Query(value = "SELECT e.id as id , e.name as name FROM emast_daily e WHERE e.status = 'A' "
            + "      AND ((:deptId IS NOT NULL AND e.department = :deptId) OR (:branchId IS NOT NULL AND e.branch= :branchId) "
            + " OR (:deptId IS NULL AND :branchId IS NULL))", nativeQuery = true)
    List<CustomVouNameByUserDeptList> findActiveLabors(@Param("deptId") Long deptId, @Param("branchId") Long branchId);

    @Query(value = "SELECT e.branch FROM emast_daily e  where e.id = :id", nativeQuery = true)
    Long findBranchIdByStaffId(@Param("id") Long conid);

    @Query(value = "SELECT e.department FROM emast_daily e where e.id = :id ", nativeQuery = true)
    Long findDeptIdByStaffId(@Param("id") Long conid);

    @Query(value = "SELECT *  FROM emast_daily e WHERE e.bioid =:bioid and e.status = 'A' ", nativeQuery = true)
    Optional<EmastDaily> findByBioIdWithActive(@Param("bioid") Long bioid);

	public boolean existsByBioid(Long bioid);
	
	public boolean existsByBioidAndIdNot(Long bioid, Long id);

}
