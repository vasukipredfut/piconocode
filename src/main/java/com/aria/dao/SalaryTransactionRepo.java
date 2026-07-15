package com.aria.dao;

import com.aria.dto.EpfSalaryList;
import com.aria.dto.EsiSalaryList;
import com.aria.dto.SalaryTransactionDetailList;
import com.aria.dto.SalaryTransactionList;
import com.aria.model.ProductList;
import com.aria.model.SalaryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryTransactionRepo extends JpaRepository<SalaryTransaction, Long> {

    @Query(value = "SELECT * FROM salary_transaction s "
            + " WHERE (:company is null OR s.cid =:company) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND (cast(:fromDate as date) is null OR s.fmperiod =:fromDate ) "
            + " AND (cast(:toDate as date)  is null OR s.toperiod =:toDate) ", nativeQuery = true)
    Page<SalaryTransaction> findAll(@Param("fromDate") Date fromDate,
                                    @Param("toDate") Date toDate,
                                    @Param("branch") Long branch,
                                    @Param("company") Long company,
                                    Pageable pageable);


    @Query(value = "SELECT * FROM salary_transaction s WHERE (:company is null OR s.cid =:company) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND (cast(:fromDate as date) is null OR s.fmperiod =:fromDate ) "
            + " AND (cast(:toDate as date)  is null OR s.toperiod =:toDate) ", nativeQuery = true)
    List<SalaryTransaction> findAll(@Param("fromDate") Date fromDate,
                                    @Param("toDate") Date toDate,
                                    @Param("branch") Long branch,
                                    @Param("company") Long company);


    @Query(value = "SELECT count(st.conid) as empCount, max(st.contable) as connectTable, SUM(st.total) as esiTotalSal, "
            + " st.cid as firmId, max(o.name) as firmName, st.bid as branchId, max(b.name) as branchName , st.trnyear as year, "
            + " st.trnmonth as month   from salary_transaction st left join organisation o on o.id = st.cid left join branch b on b.id = st.bid "
            + " WHERE st.bid > 0 AND (:company is null OR st.cid =:company) AND (:trnYear is null OR st.trnyear =:trnYear) "
            + " AND (:trnMonth is null OR st.trnmonth =:trnMonth) AND (:empType is null OR st.contable =:empType)  "
            + "group by st.cid , st.bid, st.trnyear, st.trnmonth, st.contable order by st.bid, st.trnyear desc ", nativeQuery = true)
    List<EsiSalaryList> esiSalaryList(@Param("company") Long company,
                                      @Param("trnYear") String trnYear,
                                      @Param("trnMonth") Long trnMonth,
                                      @Param("empType") String empType);


    @Query(value = "SELECT count(st.conid) as empCount, max(st.contable) as connectTable, SUM(st.total) as esiTotalSal, "
            + " st.cid as firmId, max(o.name) as firmName, st.bid as branchId, max(b.name) as branchName , st.trnyear as year, "
            + " st.trnmonth as month   from salary_transaction st left join organisation o on o.id = st.cid left join branch b on b.id = st.bid "
            + " left join epaymasternew e on e.empcode = st.conid and e.status = 'A' and e.esiexem = 'Y' "
            + " WHERE e.empcode = st.conid AND e.connect_table = st.contable AND st.bid > 0 "
            + " AND (:company is null OR st.cid =:company) AND (:trnYear is null OR st.trnyear =:trnYear) "
            + " AND (:trnMonth is null OR st.trnmonth =:trnMonth) AND (:empType is null OR st.contable =:empType)  "
            + "group by st.cid , st.bid, st.trnyear, st.trnmonth, st.contable order by st.bid, st.trnyear desc ", nativeQuery = true)
    List<EsiSalaryList> esiExemptSalaryList(@Param("company") Long company,
                                            @Param("trnYear") String trnYear,
                                            @Param("trnMonth") Long trnMonth,
                                            @Param("empType") String empType);


    @Query(value = "SELECT count(st.conid) as empCount, max(st.contable) as connectTable, SUM(st.total) as epfTotalSal, "
            + " st.cid as firmId, max(o.name) as firmName, st.bid as branchId, max(b.name) as branchName , st.trnyear as year, "
            + " st.trnmonth as month   from salary_transaction st left join organisation o on o.id = st.cid left join branch b on b.id = st.bid "
            + " WHERE st.bid > 0 AND (:company is null OR st.cid =:company) AND (:trnYear is null OR st.trnyear =:trnYear) "
            + " AND (:trnMonth is null OR st.trnmonth =:trnMonth) AND (:empType is null OR st.contable =:empType)  "
            + "group by st.cid , st.bid, st.trnyear, st.trnmonth, st.contable order by st.bid, st.trnyear desc ", nativeQuery = true)
    List<EpfSalaryList> epfSalaryList(@Param("company") Long company,
                                      @Param("trnYear") String trnYear,
                                      @Param("trnMonth") Long trnMonth,
                                      @Param("empType") String empType);

    @Query(value = "SELECT count(st.conid) as empCount, max(st.contable) as connectTable, SUM(st.total) as epfTotalSal, "
            + " st.cid as firmId, max(o.name) as firmName, st.bid as branchId, max(b.name) as branchName , st.trnyear as year, "
            + " st.trnmonth as month   from salary_transaction st left join organisation o on o.id = st.cid left join branch b on b.id = st.bid "
            + " left join epaymasternew e on e.empcode = st.conid and e.status = 'A' and e.epfexem = 'Y' "
            + " WHERE  e.empcode = st.conid AND e.connect_table = st.contable AND st.bid > 0 "
            + " AND (:company is null OR st.cid =:company) AND (:trnYear is null OR st.trnyear =:trnYear) "
            + " AND (:trnMonth is null OR st.trnmonth =:trnMonth) AND (:empType is null OR st.contable =:empType)  "
            + "group by st.cid , st.bid, st.trnyear, st.trnmonth, st.contable order by st.bid, st.trnyear desc ", nativeQuery = true)
    List<EpfSalaryList> epfExemptSalaryList(@Param("company") Long company,
                                            @Param("trnYear") String trnYear,
                                            @Param("trnMonth") Long trnMonth,
                                            @Param("empType") String empType);


    @Query(value = "SELECT * FROM salary_transaction s WHERE (:company is null OR s.cid =:company) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND ( s.trnmonth =:month) "
            + " AND ( s.contable =:conTable) "
            + " AND ( s.dacatid =:dacatId) "
            + " AND (:year is null OR s.trnyear =:year) "
            + " AND (cast(:fromDate as date) is null OR s.fmperiod =:fromDate ) "
            + " AND (cast(:toDate as date)  is null OR s.toperiod =:toDate) ", nativeQuery = true)
    List<SalaryTransaction> findBy(@Param("fromDate") Date fromDate,
                                   @Param("toDate") Date toDate,
                                   @Param("branch") Long branch,
                                   @Param("company") Long company,
                                   @Param("year") String year,
                                   @Param("month") Long month,
                                   @Param("dacatId") Long dacatId,
                                   @Param("conTable") String conTable);


    @Query(value = "SELECT * FROM salary_transaction s WHERE (:company is null OR s.cid =:company) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND ( s.contable =:conTable) "
            + " AND ( s.dacatid =:dacatId) "
            + " AND DATE(s.fmperiod) = DATE(:start) "
            + " AND DATE(s.toperiod) = DATE(:end)  LIMIT 1", nativeQuery = true)
    Optional<SalaryTransaction> findBy(@Param("branch") Long branch,
                                       @Param("company") Long company,
                                       @Param("start") Date start,
                                       @Param("end") Date end,
                                       @Param("dacatId") Long dacatId,
                                       @Param("conTable") String conTable);

    @Query(value = "SELECT * FROM salary_transaction s WHERE (:company is null OR s.cid =:company) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND ( s.contable =:conTable) "
            + " AND ( s.trnmonth =:month) "
            + " AND (:year is null OR s.trnyear =:year) LIMIT 1", nativeQuery = true)
    Optional<SalaryTransaction> findBy(@Param("branch") Long branch,
                                       @Param("company") Long company,
                                       @Param("year") String year,
                                       @Param("month") Long month,
                                       @Param("conTable") String conTable);


    @Query(value = "SELECT st.dacatid as categoryId, st.contable as connectTable, st.trnid as trnId, st.trnno as trnNo, "
            + " st.trnyear as year , st.trnmonth as month , st.bid as branchId , st.cid as companyId , max(st.did) as dept, max(b.name) as branchName ,st.status as status , "
            + " max (org.name) as companyName FROM salary_transaction st left join organisation org on org.id = st.cid "
            + " left join branch b on b.id = st.bid "
            + " WHERE (:company is null OR st.cid =:company) "
            + " AND (:branch is null OR st.bid =:branch) "
            + " AND ( :conTable is null OR st.contable =:conTable) "
            + " AND ( :dacatId is null OR st.dacatid =:dacatId) "
            + " AND ( :month is null OR st.trnmonth =:month) "
            + " AND (:year is null OR st.trnyear =:year) "
            + " AND (:status is null OR st.status =:status) "
            + " GROUP BY st.dacatid , st.contable , st.trnid , st.trnno , st.trnyear , st.trnmonth , st.bid , st.cid ,st.status ", nativeQuery = true)
    Page<SalaryTransactionList> findByList(@Param("branch") Long branch,
                                           @Param("company") Long company,
                                           @Param("year") String year,
                                           @Param("month") Long month,
                                           @Param("dacatId") Long dacatId,
                                           @Param("conTable") String conTable,
                                           @Param("status") String status,
                                           Pageable pageable);


    @Query(value = "SELECT st.dacatid as categoryId, st.contable as connectTable, st.trnid as trnId, st.trnno as trnNo, st.trnyear as year , "
            + " st.mnodays as noOfDays, st.trnmonth as month , st.bid as branchId , st.cid as companyId , b.name as branchName , org.name as companyName , "
            + " st.conid as empId , ed.employeename as empName , st.nowdays as workedDays , st.eepf1 as epf1 , st.eepf2 as epf2 , "
            + " st.basic as basic, st.da as da , st.gwa as gwa, st.hra as hra , st.wa as wa , st.conveyance as ca , st.allowance as allowance ,"
            + " st.leaveholidaybasic as leaveHolidayBasic , st.leaveholidayda as leaveHolidayDa , st.leaveholidaybasic + st.leaveholidayda  as leaveWagesBasicDa,"
            + " st.tsalary  as grossSalary, st.depf as providentFund , st.eesi as employeeWelfareFund , st.desi as employerWelfareFund,  st.mess_bill  as messBill, "
            + " st.uniformreimbursement as uniformDeduction , st.otherdeduction as otherDeduction, st.ptamount as professionalTax, "
            + " st.tds as tds , st.tdeduction as totalDeduction , st.netsalary as netSalary , st.bankacno as accountNo , st.fmperiod as fromDate ,st.toperiod as toDate, "
            + " e.bank_ifsc as ifsc , st.bank as bankName "
            + " FROM salary_transaction st left join organisation org on org.id = st.cid left join branch b on b.id = st.bid  "
            + " left join employee_details ed on ed.id =st.conid left join epaymasternew e on e.connect_table ='emaster' and e.empcode =ed.id and e.status ='A' "
            + " WHERE (:company is null OR st.cid =:company) "
            + " AND (:branch is null OR st.bid =:branch) "
            + " AND (:conTable is null OR st.contable =:conTable) "
            + " AND (:dacatId is null OR st.dacatid =:dacatId) "
            + " AND (:month is null OR st.trnmonth =:month) "
            + " AND (:trnNo is null OR st.trnno =:trnNo) "
            + " AND (:year is null OR st.trnyear =:year) ", nativeQuery = true)
    Page<SalaryTransactionDetailList> findByStaffDetailList(@Param("branch") Long branch,
                                                            @Param("company") Long company,
                                                            @Param("year") String year,
                                                            @Param("month") Long month,
                                                            @Param("dacatId") Long dacatId,
                                                            @Param("conTable") String conTable,
                                                            @Param("trnNo") String trnNo,
                                                            Pageable pageable);


    @Query(value = "SELECT st.dacatid as categoryId, st.contable as connectTable, st.trnid as trnId, st.trnno as trnNo, st.trnyear as year , "
            + " st.mnodays as noOfDays, st.trnmonth as month , st.bid as branchId , st.cid as companyId , b.name as branchName , org.name as companyName , "
            + " st.conid as empId , ed.name as empName , st.nowdays as workedDays ,st.eepf1 as epf1 , st.eepf2 as epf2 , "
            + " st.basic as basic, st.da as da , st.gwa as gwa, st.hra as hra , st.wa as wa , st.conveyance as ca , st.allowance as allowance ,"
            + " st.leaveholidaybasic as leaveHolidayBasic , st.leaveholidayda as leaveHolidayDa , st.leaveholidaybasic + st.leaveholidayda  as leaveWagesBasicDa,"
            + " st.tsalary  as grossSalary, st.depf as providentFund ,st.eesi as employeeWelfareFund , st.desi as employerWelfareFund,  st.mess_bill  as messBill, "
            + " st.uniformreimbursement as uniformDeduction , st.otherdeduction as otherDeduction, st.ptamount as professionalTax, "
            + " st.tds as tds , st.tdeduction as totalDeduction , st.netsalary as netSalary , st.bankacno as accountNo , st.fmperiod as fromDate ,st.toperiod as toDate, "
            + " e.bank_ifsc as ifsc , st.bank as bankName "
            + " FROM salary_transaction st left join organisation org on org.id = st.cid left join branch b on b.id = st.bid  "
            + " left join emast_daily ed on ed.id =st.conid left join epaymasternew e on e.connect_table ='emastdaily' and e.empcode =ed.id and e.status ='A' "
            + " WHERE (:company is null OR st.cid =:company) "
            + " AND (:branch is null OR st.bid =:branch) "
            + " AND (:conTable is null OR st.contable =:conTable) "
            + " AND (:dacatId is null OR st.dacatid =:dacatId) "
            + " AND (:month is null OR st.trnmonth =:month) "
            + " AND (:trnNo is null OR st.trnno =:trnNo) "
            + " AND (:year is null OR st.trnyear =:year) ", nativeQuery = true)
    Page<SalaryTransactionDetailList> findByWageDetailList(@Param("branch") Long branch,
                                                           @Param("company") Long company,
                                                           @Param("year") String year,
                                                           @Param("month") Long month,
                                                           @Param("dacatId") Long dacatId,
                                                           @Param("conTable") String conTable,
                                                           @Param("trnNo") String trnNo,
                                                           Pageable pageable);


    @Query(value = "SELECT  * FROM salary_transaction st   "
            + " WHERE (:company is null OR st.cid =:company) "
            + " AND (:branch is null OR st.bid =:branch) "
            + " AND (:conTable is null OR st.contable =:conTable) "
            + " AND (:dacatId is null OR st.dacatid =:dacatId) "
            + " AND (:month is null OR st.trnmonth =:month) "
            + " AND (:trnNo is null OR st.trnno =:trnNo) "
            + " AND (:year is null OR st.trnyear =:year) ", nativeQuery = true)
    List<SalaryTransaction> findByRejectList(@Param("branch") Long branch,
                                             @Param("company") Long company,
                                             @Param("year") String year,
                                             @Param("month") Long month,
                                             @Param("dacatId") Long dacatId,
                                             @Param("conTable") String conTable,
                                             @Param("trnNo") String trnNo);


    @Query(value = "SELECT st.dacatid as categoryId, st.contable as connectTable, st.trnid as trnId, st.trnno as trnNo, st.trnyear as year , "
            + " st.mnodays as noOfDays, st.trnmonth as month , st.bid as branchId , st.cid as companyId , b.name as branchName , org.name as companyName , "
            + " st.conid as empId , ed.employeename as empName , st.nowdays as workedDays ,st.eepf1 as epf1 , st.eepf2 as epf2 , "
            + " st.basic as basic, st.da as da , st.gwa as gwa, st.hra as hra , st.wa as wa , st.conveyance as ca , st.allowance as allowance ,"
            + " st.leaveholidaybasic as leaveHolidayBasic , st.leaveholidayda as leaveHolidayDa , st.leaveholidaybasic + st.leaveholidayda  as leaveWagesBasicDa,"
            + " st.tsalary  as grossSalary, st.depf as providentFund , st.eesi as employeeWelfareFund , st.desi as employerWelfareFund,  st.mess_bill  as messBill, "
            + " st.uniformreimbursement as uniformDeduction , st.otherdeduction as otherDeduction, st.ptamount as professionalTax, "
            + " st.tds as tds , st.tdeduction as totalDeduction , st.netsalary as netSalary , st.bankacno as accountNo ,st.fmperiod as fromDate ,st.toperiod as toDate, "
            + " e.bank_ifsc as ifsc , st.bank as bankName "
            + " FROM salary_transaction st left join organisation org on org.id = st.cid left join branch b on b.id = st.bid  "
            + " left join employee_details ed on ed.id =st.conid left join epaymasternew e on e.connect_table ='emaster' and e.empcode =ed.id and e.status ='A' "
            + " WHERE (:company is null OR st.cid =:company) "
            + " AND (:branch is null OR st.bid =:branch) "
            + " AND (:conTable is null OR st.contable =:conTable) "
            + " AND (:dacatId is null OR st.dacatid =:dacatId) "
            + " AND (:month is null OR st.trnmonth =:month) "
            + " AND (:trnNo is null OR st.trnno =:trnNo) "
            + " AND (:year is null OR st.trnyear =:year) ", nativeQuery = true)
    List<SalaryTransactionDetailList> findByStaffDetailList(@Param("branch") Long branch,
                                                            @Param("company") Long company,
                                                            @Param("year") String year,
                                                            @Param("month") Long month,
                                                            @Param("dacatId") Long dacatId,
                                                            @Param("conTable") String conTable,
                                                            @Param("trnNo") String trnNo);


    @Query(value = "SELECT st.dacatid as categoryId, st.contable as connectTable, st.trnid as trnId, st.trnno as trnNo, st.trnyear as year , "
            + " st.mnodays as noOfDays, st.trnmonth as month , st.bid as branchId , st.cid as companyId , b.name as branchName , org.name as companyName , "
            + " st.conid as empId , ed.name as empName , st.nowdays as workedDays ,st.eepf1 as epf1 , st.eepf2 as epf2 , "
            + " st.basic as basic, st.da as da , st.gwa as gwa, st.hra as hra , st.wa as wa , st.conveyance as ca , st.allowance as allowance ,"
            + " st.leaveholidaybasic as leaveHolidayBasic , st.leaveholidayda as leaveHolidayDa , st.leaveholidaybasic + st.leaveholidayda  as leaveWagesBasicDa,"
            + " st.tsalary  as grossSalary, st.depf as providentFund , st.eesi as employeeWelfareFund , st.desi as employerWelfareFund,  st.mess_bill  as messBill, "
            + " st.uniformreimbursement as uniformDeduction , st.otherdeduction as otherDeduction, st.ptamount as professionalTax, "
            + " st.tds as tds , st.tdeduction as totalDeduction , st.netsalary as netSalary , st.bankacno as accountNo ,st.fmperiod as fromDate ,st.toperiod as toDate, "
            + " e.bank_ifsc as ifsc , st.bank as bankName "
            + " FROM salary_transaction st left join organisation org on org.id = st.cid left join branch b on b.id = st.bid  "
            + " left join emast_daily ed on ed.id =st.conid left join epaymasternew e on e.connect_table ='emastdaily' and e.empcode =ed.id and e.status ='A' "
            + " WHERE (:company is null OR st.cid =:company) "
            + " AND (:branch is null OR st.bid =:branch) "
            + " AND (:conTable is null OR st.contable =:conTable) "
            + " AND (:dacatId is null OR st.dacatid =:dacatId) "
            + " AND (:month is null OR st.trnmonth =:month) "
            + " AND (:trnNo is null OR st.trnno =:trnNo) "
            + " AND (:year is null OR st.trnyear =:year) ", nativeQuery = true)
    List<SalaryTransactionDetailList> findByWageDetailList(@Param("branch") Long branch,
                                                           @Param("company") Long company,
                                                           @Param("year") String year,
                                                           @Param("month") Long month,
                                                           @Param("dacatId") Long dacatId,
                                                           @Param("conTable") String conTable,
                                                           @Param("trnNo") String trnNo);


    @Query(value = "SELECT COUNT(st.id) as count FROM salary_transaction st  WHERE (:company is null OR st.cid =:company) "
            + " AND (:branch is null OR st.bid =:branch) "
            + " AND (:conTable is null OR st.contable =:conTable) "
            + " AND (:dacatId is null OR st.dacatid =:dacatId) "
            + " AND (:sex is null OR st.sex =:sex) "
            + " AND (:month is null OR st.trnmonth =:month) "
            + " AND (:trnNo is null OR st.trnno =:trnNo) "
            + " AND (:year is null OR st.trnyear =:year) ", nativeQuery = true)
    public int countValue(@Param("branch") Long branch,
                             @Param("company") Long company,
                             @Param("year") String year,
                             @Param("month") Long month,
                             @Param("dacatId") Long dacatId,
                             @Param("conTable") String conTable,
                             @Param("sex") String sex,
                             @Param("trnNo") String trnNo);


    @Query(value = "SELECT COUNT(st.conid) as count FROM salary_transaction st left join organisation org on org.id = st.cid left join branch b on b.id = st.bid  "
            + " left join emast_daily ed on ed.id =st.conid left join epaymasternew e on e.connect_table ='emastdaily' and e.empcode =ed.id and e.status ='A' "
            + " WHERE (:company is null OR st.cid =:company) "
            + " AND (:branch is null OR st.bid =:branch) "
            + " AND (:conTable is null OR st.contable =:conTable) "
            + " AND (:dacatId is null OR st.dacatid =:dacatId) "
            + " AND (:sex is null OR st.sex =:sex) "
            + " AND (:month is null OR st.trnmonth =:month) "
            + " AND (:trnNo is null OR st.trnno =:trnNo) "
            + " AND (:year is null OR st.trnyear =:year) ", nativeQuery = true)
    public int wagesCountValue(@Param("branch") Long branch,
                                  @Param("company") Long company,
                                  @Param("year") String year,
                                  @Param("month") Long month,
                                  @Param("dacatId") Long dacatId,
                                  @Param("conTable") String conTable,
                                  @Param("sex") String sex,
                                  @Param("trnNo") String trnNo);


    @Query(value = "SELECT * FROM salary_transaction s WHERE (:company is null OR s.cid =:company) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND ( s.contable =:conTable) "
            + " AND ( s.trnmonth =:month) "
            + " AND ( cast(:startDate date) is null OR s.fmperiod =:startDate ) "
            + " AND ( cast(:endDate date) is null OR s.toperiod =:endDate) "
            + " AND (:year is null OR s.trnyear =:year) LIMIT 1", nativeQuery = true)
    Optional<SalaryTransaction> findBy(@Param("branch") Long branch,
                                       @Param("company") Long company,
                                       @Param("year") String year,
                                       @Param("month") Long month,
                                       @Param("conTable") String conTable,
                                       @Param("startDate") Date startDate,
                                       @Param("endDate") Date endDate);



    @Query(value = "SELECT * FROM salary_transaction s WHERE (:company is null OR s.cid =:company) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND ( s.contable =:conTable) "
            + " AND ( s.salarytype =:SalaryType) "
            + " AND (:dept is null OR s.did =:dept) ORDER BY s.id DESC LIMIT 1", nativeQuery = true)
    Optional<SalaryTransaction> findBy(@Param("company") Long company,
                                       @Param("branch") Long branch,
                                       @Param("dept") Long dept,
                                       @Param("conTable") String conTable,
                                       @Param("SalaryType") String SalaryType);
    
    @Query(value = "SELECT * FROM salary_transaction s WHERE s.conid = :empId " +
    	       " AND s.trnyear = :year " +
    	       " AND s.trnmonth >= :startMonth " +
    	       " AND s.trnmonth <= :endMonth  AND ( s.contable =:conTable) " +
    	       " AND s.desi > 0 ORDER BY s.trnmonth ASC LIMIT 1" , nativeQuery = true)
    	Optional<SalaryTransaction> findEsiDone(  @Param("empId") Long empId,  @Param("year") String year, @Param("startMonth") int startMonth, @Param("endMonth") int endMonth, @Param("conTable") String conTable );


}
