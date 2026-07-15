package com.aria.dao;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.VoucherEntryList;
import com.aria.dto.VoucherPrintDto;
import com.aria.dto.VoucherPrintList;
import com.aria.model.VoucherEntry;

@Repository
public interface VoucherEntryRepository  extends JpaRepository<VoucherEntry, Long>{
	
	@Query(value= "SELECT sa.shift_id AS shiftId "
			+ " FROM shift_assign sa	LEFT JOIN shift_rotation sr ON sr.id = sa.rotation_id "
			+ "	LEFT JOIN shifts sh ON sh.id = sa.shift_id "
			+ "	LEFT JOIN emast_daily ed ON ed.id = sa.emp_id "
			+ "	WHERE sa.emp_type = 1  AND sa.emp_id = :empid "
			+ "	  AND sa.shift_date =:shift_date ORDER BY sa.shift_date " , nativeQuery=true) 
	Long findByShiftId( @Param("shift_date")LocalDate shift_date ,@Param("empid") Long empid);
	
	
	@Query(value= " SELECT vf.isono from vouformat vf LEFT JOIN vouname v ON v.voufirmid=vf.id where v.id=:vouid " ,  nativeQuery=true)
	String findByIsono(@Param("vouid") Long vouid);

	@Query(value= " SELECT b.trnprefix  from branch b  where b.id=:branch_id " ,  nativeQuery=true)
	String findByUserId(@Param("branch_id") Long branch_id);

	@Query(value= " SELECT v.prefix from  vouname v  where v.id=:vouid " ,  nativeQuery=true)
	String findByVouId(@Param("vouid") Long vouid);


	@Query(value= "SELECT v.id AS id, v.vouno AS vouNo, v.voutype AS vouType, v.voudt AS vouDate, v.status,"
			+ "			    vn.name AS cVouName ,v.grpno AS grpNo"
			+ "				FROM vouchers v  LEFT JOIN vouname vn ON vn.id = v.formid "
			+ "			   LEFT JOIN vouitems vo ON vo.vouid = v.id "
			+ "		  WHERE(:formId IS NULL OR v.company = :formId ) AND (:orgId IS NULL OR v.company = :orgId ) AND (:deptId IS NULL OR v.dept = :deptId ) AND (:branchId IS NULL OR v.branch = :branchId ) AND(:dept IS NULL OR v.dept = :dept) AND (:branch IS NULL OR v.branch = :branch) "
			+ "		AND (cast(:fromDate as date) is null or (v.voudt  = :fromDate))  AND (:status IS NULL OR lower(v.status) LIKE ('%' || lower(:status) || '%')) AND (:grpNo IS NULL OR lower(v.grpno) LIKE ('%' || lower(:grpNo) || '%'))"
			+ " AND (:vouNo IS NULL OR lower(v.vouno) LIKE ('%' || lower(:vouNo) || '%'))AND (:vouType IS NULL OR lower(v.voutype) LIKE ('%' || lower(:vouType) || '%')) AND (:vouName IS NULL OR lower(vn.name) LIKE ('%' || lower(:vouName) || '%')) GROUP BY v.id, v.vouno, v.voutype, v.voudt, vn.name ORDER BY v.id DESC "
				 ,  nativeQuery=true)
	Page<VoucherEntryList> filterVoucherList(@Param("formId") Long formId, @Param("dept") Long dept, @Param("branch") Long branch,@Param("status") String status ,@Param("deptId") Long deptId ,@Param("branchId") Long branchId,@Param("orgId") Long orgId,@Param("grpNo") String grpNo,@Param("vouNo") String vouNo,@Param("vouType") String vouType,@Param("vouName") String vouName,@Param("fromDate") Date fromDate, Pageable pageable);
	
	@Query(value= "SELECT * FROM vouchers v WHERE ( :branch IS NULL OR v.branch = :branch ) "
			+ "  AND ( :dept IS NULL OR v.dept = :dept ) "
			+ "  AND ( :formId IS NULL OR v.company = :formId ) "
			+ "  AND ( :vouId IS NULL OR v.formid = :vouId ) "
			+ " AND (cast(:fromDate as date) is null or (v.voudt  >= :fromDate)) "
			+ " AND (cast(:toDate as date) is null or (v.voudt <= :toDate)) "
			+ "      AND v.status = 'Verified' AND v.grpno is NULL " ,  nativeQuery=true )
	List<VoucherEntry> filterVoucherListGroup(@Param("vouId") Long vouId,@Param("formId") Long formId,@Param("dept") Long dept,@Param("branch") Long branch,@Param("fromDate") Date fromDate,@Param("toDate") Date toDate);

	@Query(value= " SELECT * from vouchers v where v.grpno=:grpNo " ,  nativeQuery=true)
	List<VoucherEntry> findByGrpno(@Param("grpNo") String grpNo);

	@Query(value= "SELECT v.id AS id , v.vouno AS VoucherNo ,v.istaff AS empId ,v.amt AS amount , v.payabletoname AS payTo , v.pmode AS payMode , v.voutype AS vouType , "
			+ "			emp.employeename AS createdPerson , v.idate AS createdDate , v.vdate AS verifiedDate , "
			+ "			 vemp.employeename AS verifiedPerson,	v.apdate AS approvedDate , apemp.employeename AS approvedEmployee ,v.adate AS authorizedDate , auemp.employeename AS authorizedPerson ,\r\n"
			+ "			 o.name AS companyName , o.address AS cAddress ,b.name AS branchName ,v.rby AS rejectedDate , remp.employeename AS rejectedPerson , "
			+ "			v.voudt AS voucherDate , MAX(vx.description) AS description, v.bankcode AS bankName , v.bankacno AS bankAcNo , v.ifsccode AS ifscCode "
			+ "		 FROM vouchers v left join vexpdes vx ON vx.bid=v.id  left join employee_details emp ON emp.id = v.istaff "
			+ "		 left join employee_details vemp ON vemp.id = v. vstaff left join employee_details apemp ON apemp.id = v.apstaff left join employee_details auemp ON auemp.id = v.astaff left join employee_details remp ON remp.id = v.rby "
			+ "		   left join organisation o ON o.id = v.company left join branch b ON b.id = v.branch  where v.id = :id group by v.id, "
			+ "    v.vouno,v.amt,v.payabletoname,v.pmode,emp.employeename,v.idate,v.vdate,vemp.employeename,v.apdate,apemp.employeename, remp.employeename , v.rdate ,v.adate,auemp.employeename,o.name,o.address,b.name,v.voudt,v.bankcode,v.bankacno,v.ifsccode , v.voutype " ,  nativeQuery=true )
	Optional<VoucherPrintList> findPrintByVouid(@Param("id") Long id);

	@Query(value= " SELECT v.id from vouchers v where v.grpno=:grpNo " ,  nativeQuery=true)
	List<Long> findByGrpnoList(String grpNo);

	@Query(value= " SELECT * from vouchers v where v.grpno= :grpNo  limit 1 ",  nativeQuery=true)
	Optional<VoucherEntry> findByVoucherName(@Param("grpNo")String grpNo);
	

}
