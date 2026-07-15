package com.aria.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.OtherDeductionList;
import com.aria.dto.SalesvraBillList;
import com.aria.dto.SalesvraList;
import com.aria.dto.SalevraBillListDto;
import com.aria.model.Salesvra;

@Repository
public interface SalesvraRepo extends JpaRepository<Salesvra, Long> {

	@Query(value = "select count(*) from salesvra s", nativeQuery = true)
	public int findTotalCount();

	@Query(value = "select s.id as id, s.billno as billno,ed.employeename as employeename,s.totable  as emptype,s.trnno as trnno,\r\n"
			+ "			b.name as branch,o.name as company,dd.name as dept,s.bill_month  as month,s.bill_year as year,s.description as deductiontype \r\n"
			+ "			from salesvra s left join employee_details ed on ed.id = CAST(NULLIF(regexp_replace(s.party, '[^0-9]', '', 'g'), '') AS bigint)\r\n"
			+ "			left join branch b on b.id=s.bid left join organisation o on o.id=s.cid left join department_details dd on dd.id = s.did \r\n"
			+ "			where (:contable is null or s.totable=:contable) and (:month is null or s.bill_month = :month)and (:year is null or s.bill_year = :year) \r\n"
			+ "			and (:billno is null or s.billno = :billno)and (:branch is null or s.bid = :branch) and s.mess_voucher is null\r\n"
			+ "			and (:employeename is null or (lower(ed.employeename) LIKE ('%' || lower(:employeename) || '%'))) \r\n"
			+ "			and (:company is null or s.cid = :company)and (:dept is null or s.did = :dept)\r\n"
			+ "			and (:deductiontype is null or (lower(s.description) LIKE ('%' || lower(:deductiontype) || '%'))) order by s.id desc", nativeQuery = true)
	Page<SalesvraList> findByStaff(@Param("employeename") String employeename, @Param("billno") String billno,
			@Param("deductiontype") String type, @Param("branch") Long branch, @Param("dept") Long dept,
			@Param("company") Long company, @Param("month") Long month, @Param("year") Long year,
			@Param("contable") String contable, Pageable pageable);

	@Query(value = "select s.id as id,s.billno as billno,ed.name as employeename,s.totable as emptype,s.trnno as trnno,\r\n"
			+ "			b.name as branch,o.name as company,dd.name as dept,s.bill_month  as month,s.bill_year as year,s.description as deductiontype \r\n"
			+ "			from salesvra s left join emast_daily  ed on ed.id = CAST(NULLIF(regexp_replace(s.party, '[^0-9]', '', 'g'), '') AS bigint) \r\n"
			+ "			left join branch b on b.id=s.bid left join organisation o on o.id=s.cid left join department_details dd on dd.id = s.did \r\n"
			+ "			where (:contable is null or s.totable=:contable) and (:month is null or s.bill_month = :month)and (:year is null or s.bill_year = :year) \r\n"
			+ "			and (:billno is null or s.billno = :billno)and (:branch is null or s.bid = :branch) and s.mess_voucher is null\r\n"
			+ "			and (:employeename is null or (lower(ed.name) LIKE ('%' || lower(:employeename) || '%'))) \r\n"
			+ "			and (:company is null or s.cid = :company)and (:dept is null or s.did = :dept) \r\n"
			+ "			and (:deductiontype is null or (lower(s.description) LIKE ('%' || lower(:deductiontype) || '%'))) order by s.id desc", nativeQuery = true)
	Page<SalesvraList> findByWages(@Param("employeename") String employeename, @Param("billno") String billno,
			@Param("deductiontype") String type, @Param("branch") Long branch, @Param("dept") Long dept,
			@Param("company") Long company, @Param("month") Long month, @Param("year") Long year,
			@Param("contable") String contable, Pageable pageable);

	@Query(value = "select s.id as Id,s.billno as billno,s.billdt as billdate,ed.employeename as employeename, s.description as deductiontype,"
			+ "s.trnno as trnno,s.amt as amount from salesvra s left join employee_details ed "
			+ "on ed.id=s.bstaff  where (:month is null or s.bill_month =:month) and (:year is null or s.bill_year =:year) "
			+ "and (:branch is null or s.bid=:branch) and (:company is null or s.cid=:company)", nativeQuery = true)
	List<SalesvraBillList> findBills(@Param("month") Long month, @Param("year") Long year, @Param("branch") Long branch,
			@Param("company") Long company);

	@Query(value = "SELECT \r\n" + "    s.id as id,\r\n" + "    s.billno as billno,\r\n"
			+ "    s.billdt as billdate,\r\n" + "    COALESCE(ed.employeename, edd.name) AS employeename,\r\n"
			+ "    s.description AS deductiontype,\r\n" + "    org.name AS companyname,\r\n"
			+ "    org.address AS caddress,\r\n" + "    s.trnno,\r\n" + "    s.amt,\r\n" + "    org.city AS ccity,\r\n"
			+ "    b.name AS plant,\r\n" + "    st.cgst,\r\n" + "    st.sgst,\r\n" + "    st.igst\r\n"
			+ "FROM salesvra s\r\n" + "LEFT JOIN branch b ON b.id = s.bid\r\n" + "\r\n"
			+ "LEFT JOIN employee_details ed\r\n" + "ON split_part(s.party, '.', 2) = 'emaster'\r\n"
			+ "AND ed.id = CAST(\r\n"
			+ "    NULLIF(regexp_replace(split_part(s.party, '.', 1), '[^0-9]', '', 'g'), '')\r\n" + "    AS bigint\r\n"
			+ ")\r\n" + "\r\n" + "LEFT JOIN emast_daily edd\r\n" + "ON split_part(s.party, '.', 2) = 'emastdaily'\r\n"
			+ "AND edd.id = CAST(\r\n"
			+ "    NULLIF(regexp_replace(split_part(s.party, '.', 1), '[^0-9]', '', 'g'), '')\r\n" + "    AS bigint\r\n"
			+ ")\r\n" + "\r\n" + "LEFT JOIN organisation org ON org.id = s.cid\r\n"
			+ "LEFT JOIN salesvraitems st ON st.trnno = s.trnno\r\n" + "\r\n" + "WHERE\r\n"
			+ "    (:month IS NULL OR s.bill_month = :month)\r\n" + "    AND (:year IS NULL OR s.bill_year = :year)\r\n"
			+ "    AND (:branch IS NULL OR s.bid = :branch)\r\n" + "    AND (:company IS NULL OR s.cid = :company)\r\n"
			+ "    AND s.mess_voucher IS NULL;", nativeQuery = true)
	List<SalesvraBillList> findBill(@Param("month") Long month, @Param("year") Long year, @Param("branch") Long branch,
			@Param("company") Long company);

	@Query(value = "SELECT \r\n" + "    sales.trnno AS invoiceno,\r\n" + "    sales.billdt AS billdate,\r\n"
			+ "    sales.amt AS rate,\r\n" + "    sales.amt AS rs,\r\n" + "    sales.roundoff AS roundoff,\r\n" + "\r\n"
			+ "    org.gst AS gst,\r\n" + "    org.regn_1 AS cst,\r\n" + "    org.phone AS phone,\r\n"
			+ "    org.name AS company,\r\n" + "    org.city AS ccity,\r\n" + "    org.address AS caddress,\r\n"
			+ "\r\n" + "    COALESCE(ed.employeename, edd.name) AS employeename,\r\n"
			+ "    COALESCE(ed.address, edd.address) AS eaddress,\r\n" + "\r\n" + "    salesitem.qty AS qty,\r\n"
			+ "    salesitem.cgst AS cgst,\r\n" + "    salesitem.sgst AS sgst,\r\n" + "    salesitem.igst AS igst,\r\n"
			+ "\r\n" + "    sales.description AS patriclur,\r\n" + "    p.hsn_ref_id AS hsncode\r\n" + "\r\n"
			+ "FROM salesvra sales\r\n" + "\r\n" + "LEFT JOIN organisation org \r\n" + "    ON org.id = sales.cid\r\n"
			+ "\r\n" + "-- ==============================\r\n" + "-- PARTY = emaster\r\n"
			+ "-- ==============================\r\n" + "LEFT JOIN employee_details ed\r\n"
			+ "    ON split_part(sales.party, '.', 2) = 'emaster'\r\n" + "    AND ed.id = CAST(\r\n"
			+ "        NULLIF(\r\n"
			+ "            regexp_replace(split_part(sales.party, '.', 1), '[^0-9]', '', 'g'),\r\n"
			+ "            ''\r\n" + "        ) AS bigint\r\n" + "    )\r\n" + "\r\n"
			+ "-- ==============================\r\n" + "-- PARTY = emastdaily\r\n"
			+ "-- ==============================\r\n" + "LEFT JOIN emast_daily edd\r\n"
			+ "    ON split_part(sales.party, '.', 2) = 'emastdaily'\r\n" + "    AND edd.id = CAST(\r\n"
			+ "        NULLIF(\r\n"
			+ "            regexp_replace(split_part(sales.party, '.', 1), '[^0-9]', '', 'g'),\r\n"
			+ "            ''\r\n" + "        ) AS bigint\r\n" + "    )\r\n" + "\r\n"
			+ "LEFT JOIN salesvraitems salesitem \r\n" + "    ON salesitem.trnno = sales.trnno\r\n" + "\r\n"
			+ "LEFT JOIN products_master p \r\n" + "    ON p.id = salesitem.refid\r\n" + "\r\n"
			+ "WHERE sales.id IN (:ids)", nativeQuery = true)
	List<SalevraBillListDto> findBillPrintDetailsByIds(@Param("ids") List<Long> ids);

	@Query(value = "SELECT * FROM salesvra WHERE id IN (:ids)", nativeQuery = true)
	List<Salesvra> findBillsByIds(@Param("ids") List<Long> ids);

	List<Salesvra> findByMessvoucher(Long messvoucher);

}
