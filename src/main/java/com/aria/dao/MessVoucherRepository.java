package com.aria.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.MessVoucherList;
import com.aria.dto.MessVoucherPrintList;
import com.aria.model.MessVoucher;

@Repository
public interface MessVoucherRepository extends JpaRepository<MessVoucher, Long> {

	@Query(value = "SELECT COALESCE(MAX(voucherno), 0) FROM mess_voucher", nativeQuery = true)
	Long getMaxVoucherNo();

	@Query(value = "select v.id as id, v.voucherno as voucherno, v.month as month, v.year as year,b.name as branch, org.name as company, v.status as status \r\n"
			+ "			from mess_voucher v left join branch b on b.id = v.branch left join organisation org on org.id=v.cid \r\n"
			+ "			where (:voucherno is null or v.voucherno = :voucherno) and (v.status !='Rejected')\r\n"
			+ "			and (:month is null or v.month = :month) and (:year is null or v.year = :year) \r\n"
			+ "			and (:branch is null or v.branch = :branch) and (:company is null or v.cid = :company) \r\n"
			+ "			and (:status is null or v.status = :status) order by v.voucherno desc", nativeQuery = true)
	Page<MessVoucherList> findVoucher(@Param("voucherno") Long voucherno, @Param("month") Long month,
			@Param("year") Long year, @Param("branch") Long branch, @Param("company") Long company,
			@Param("status") String status, Pageable pageable);

	@Query(value = "SELECT \r\n" + "    s.id AS id,\r\n" + "    s.billno AS billno,\r\n"
			+ "    s.billdt AS billdate,\r\n" + "    COALESCE(ed.employeename, edd.name) AS employeename,\r\n"
			+ "    s.description AS deductiontype,\r\n" + "    org.name AS companyname,\r\n"
			+ "    org.address AS caddress,\r\n" + "    s.trnno AS trnno,\r\n" + "    s.amt AS amount,\r\n"
			+ "    org.city AS ccity,\r\n" + "    b.name AS plant,\r\n" + "    v.voucherno AS voucherno,\r\n"
			+ "    v.month AS month,\r\n" + "    v.year AS year,\r\n" + "    v.voucherdate AS date,\r\n"
			+ "    e1.employeename AS createdByName,\r\n" + "    e2.employeename AS verifiedByName,\r\n"
			+ "    e3.employeename AS approvedByName,\r\n" + "    st.cgst AS cgst,\r\n" + "    st.sgst AS sgst,\r\n"
			+ "    st.igst AS igst\r\n" + "FROM salesvra s\r\n" + "LEFT JOIN branch b \r\n" + "    ON b.id = s.bid\r\n"
			+ "LEFT JOIN employee_details ed\r\n" + "    ON split_part(s.party, '.', 2) = 'emaster'\r\n"
			+ "    AND ed.id = CAST(\r\n" + "        NULLIF(\r\n"
			+ "            regexp_replace(split_part(s.party, '.', 1), '[^0-9]', '', 'g'),\r\n" + "            ''\r\n"
			+ "        ) AS bigint\r\n" + "    )\r\n" + "LEFT JOIN emast_daily edd\r\n"
			+ "    ON split_part(s.party, '.', 2) = 'emastdaily'\r\n" + "    AND edd.id = CAST(\r\n"
			+ "        NULLIF(\r\n" + "            regexp_replace(split_part(s.party, '.', 1), '[^0-9]', '', 'g'),\r\n"
			+ "            ''\r\n" + "        ) AS bigint\r\n" + "    )\r\n" + "LEFT JOIN organisation org \r\n"
			+ "    ON org.id = s.cid\r\n" + "LEFT JOIN mess_voucher v \r\n" + "    ON v.voucherno = s.mess_voucher\r\n"
			+ "LEFT JOIN employee_details e1 \r\n" + "    ON e1.id = v.createby\r\n"
			+ "LEFT JOIN employee_details e2 \r\n" + "    ON e2.id = v.verifyby\r\n"
			+ "LEFT JOIN employee_details e3 \r\n" + "    ON e3.id = v.approveby\r\n"
			+ "LEFT JOIN salesvraitems st \r\n" + "    ON st.trnno = s.trnno\r\n"
			+ "WHERE v.id IN (:ids)", nativeQuery = true)
	List<MessVoucherPrintList> findVoucherForPrint(@Param("ids") List<Long> ids);

	@Query(value = "SELECT \r\n" + "    s.id AS id,\r\n" + "    s.billno AS billno,\r\n"
			+ "    s.billdt AS billdate,\r\n" + "    COALESCE(ed.employeename, edd.name) AS employeename,\r\n"
			+ "    s.description AS deductiontype,\r\n" + "    org.name AS companyname,\r\n"
			+ "    org.address AS caddress,\r\n" + "    s.trnno AS trnno,\r\n" + "    s.amt AS amount,\r\n"
			+ "    org.city AS ccity,\r\n" + "    b.name AS plant,\r\n" + "    v.voucherno AS voucherno,\r\n"
			+ "    v.month AS month,\r\n" + "    v.year AS year,\r\n" + "    v.voucherdate AS date,\r\n"
			+ "    e1.employeename AS createdByName,\r\n" + "    e2.employeename AS verifiedByName,\r\n"
			+ "    e3.employeename AS approvedByName,\r\n" + "    st.cgst AS cgst,\r\n" + "    st.sgst AS sgst,\r\n"
			+ "    st.igst AS igst\r\n" + "FROM salesvra s\r\n" + "LEFT JOIN branch b \r\n" + "    ON b.id = s.bid\r\n"
			+ "LEFT JOIN employee_details ed\r\n" + "    ON split_part(s.party, '.', 2) = 'emaster'\r\n"
			+ "    AND ed.id = CAST(\r\n" + "        NULLIF(\r\n"
			+ "            regexp_replace(split_part(s.party, '.', 1), '[^0-9]', '', 'g'),\r\n" + "            ''\r\n"
			+ "        ) AS bigint\r\n" + "    )\r\n" + "LEFT JOIN emast_daily edd\r\n"
			+ "    ON split_part(s.party, '.', 2) = 'emastdaily'\r\n" + "    AND edd.id = CAST(\r\n"
			+ "        NULLIF(\r\n" + "            regexp_replace(split_part(s.party, '.', 1), '[^0-9]', '', 'g'),\r\n"
			+ "            ''\r\n" + "        ) AS bigint\r\n" + "    )\r\n" + "LEFT JOIN organisation org \r\n"
			+ "    ON org.id = s.cid\r\n" + "LEFT JOIN mess_voucher v \r\n" + "    ON v.voucherno = s.mess_voucher\r\n"
			+ "LEFT JOIN employee_details e1 \r\n" + "    ON e1.id = v.createby\r\n"
			+ "LEFT JOIN employee_details e2 \r\n" + "    ON e2.id = v.verifyby\r\n"
			+ "LEFT JOIN employee_details e3 \r\n" + "    ON e3.id = v.approveby\r\n"
			+ "LEFT JOIN salesvraitems st \r\n" + "    ON st.trnno = s.trnno\r\n" + "WHERE v.id =:id ", nativeQuery = true)
	List<MessVoucherPrintList> findVoucherById(@Param("id") Long id);

}
