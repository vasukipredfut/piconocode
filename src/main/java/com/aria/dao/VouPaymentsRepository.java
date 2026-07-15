
package com.aria.dao;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.dto.VoucherGroupPrintDto;
import com.aria.dto.VoucherGroupPrintList;
import com.aria.dto.VoucherGrpList;
import com.aria.model.VouPayments;

@Repository
public interface VouPaymentsRepository extends JpaRepository<VouPayments, Long>{

	
	@Query(value = "SELECT  v.id AS id, v.trnno AS grpNo , v.fmdate AS fmDate , v.todate AS toDate, v.status AS status , b.name AS bName , d.name AS dName , o.name AS cName FROM voupayments v LEFT JOIN department_details d ON v.did = d.id "
			+ " LEFT JOIN branch b ON b.id = v.bid  LEFT JOIN  organisation o ON o.id = v.cid "	
			+ " where (:formid IS NULL OR o.id = :formid) AND (:branch IS NULL OR b.id = :branch) "
			+ "    AND (:dept IS NULL OR d.id = :dept) AND (:grpNo IS NULL OR v.trnno = :grpNo) "
			+ " AND (:orgId IS NULL OR v.cid = :orgId ) AND (:deptId IS NULL OR v.did = :deptId ) AND (:branchId IS NULL OR v.bid = :branchId )"
			+ " AND (:status is null or (lower(v.status) LIKE ('%' || lower(:status) || '%'))) "
			+ " AND (cast(:fromDate as date) is null or (v.fmdate  >= :fromDate)) "
			+ " AND (cast(:toDate as date) is null or (v.todate <= :toDate)) order by v.id desc", nativeQuery = true)
	Page<VoucherGrpList> GetFilterList(Long formid, Long dept, Long branch, String grpNo, Date fromDate, Date toDate, String status,
			Long deptId , Long branchId , Long orgId ,Pageable pageable);

	@Query(value= "SELECT v.id AS id , v.trnno AS grpNo ,v.fmdate AS fmDate , v.todate AS toDate , v.trndate AS vouDate ,v.vouid AS cVouId  ,cemp.employeename AS createdPerson ,v.con AS createdDate ,vemp.employeename AS verifiedPerson ,v.von AS verifiedDate ,apemp.employeename AS approvedPerson ,v.apon AS approvedDate , aemp.employeename AS authorizedPerson,v.aon AS authorizededDate , remp.employeename AS rejectedPerson ,v.rDate AS rejectedDate ,v.connew as newCreatedDate ,v.aponnew AS newApprovedDate ,v.vonnew AS newVerfiedDate , v.aonnew AS newAuthorizedDate ,"
			 + " c.name AS companyName , c.address AS cAddress ,b.name AS branchName FROM voupayments  v  "			
		+ "	left join employee_details cemp ON cemp.id = v.cby left join employee_details vemp ON vemp.id = v.vby left join employee_details apemp ON apemp.id = v.apby left join employee_details aemp ON aemp.id = v.aby left join employee_details remp ON remp.id = v.rby	   left join organisation c ON c.id = v.cid left join branch b ON b.id = v.bid left join vouname vn ON vn.id = v.vouid where v.id = :id group by  v.aon,vemp.employeename, aemp.employeename, v.rDate,v.id,v.trnno, v.apon ,v.connew,remp.employeename  ,"
		+ "v.con, apemp.employeename, v.fmdate, v.todate,v.von, v.trndate, v.vouid, c.name,v.aonnew,v.vonnew,v.aponnew, c.address, b.name,vemp.employeename, v.con, cemp.employeename " ,  nativeQuery=true )
	VoucherGroupPrintList findPrintByGroupId(Long id);


	

	

}
