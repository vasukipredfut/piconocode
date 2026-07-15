package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;

import com.aria.dto.*;
import com.aria.model.QcInProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QcInProcessRepository extends JpaRepository<QcInProcess, Long> {

    @Query(value = "SELECT cm FROM QcInProcess cm WHERE " + " (:id is null or cm.id = :id) ")
    Optional<QcInProcess> findQcInProcessData(@Param("id") Long id);

    @Query(value = "select  batchno as batchNo, max(id) as patId,  max(kpi_name_id) as kpiNameId, max(shift_date) as inwardDate, "
            + "max(kpiName) as kpiName from " + "( "
            + "select pat.id, batchno, kpi_name_id, shift_date, kpi.name as kpiName "
            + "	from in_process_data pat, kpiname as kpi where kpi.id=pat.kpi_name_id order by id desc  "
            + ") as t1  group by batchno order by patId desc ", countQuery = "select count(q1.*) from "
            + "(select  batchno as batchNo, max(id) as patId,  max(kpi_name_id) as kpiNameId, max(shift_date) as inwardDate, "
            + "			max(kpiName) as kpiName from " + "			( "
            + "			select pat.id, batchno, kpi_name_id, shift_date, kpi.name as kpiName "
            + "			from attribute_fields_inprocess pat, kpiname as kpi where kpi.id=pat.kpi_name_id order by id desc "
            + "			) as t1  group by batchno order by patId desc) as q1", nativeQuery = true)
    Page<Tuple> findAttributeFieldsInProcessData(Pageable pageable);

    @Query(value = "select  inprocessdetails.id as patId, " + "tg.taskgroup as taskGroupId,  "
            + "astep.task_category_id as taskCategoryId,astep.task_name_id as taskNameId, "
            + "tg.task_group_name taskGroupName,  " + "			tn.task_name taskName,  "
            + "			(astep.id)  stepId, astep.step_number as stepNumber,  "
            + "			inprocessdetails.attributes_fields_process_type_id as attributeId,att.name as attributeName,  "
            + "			inprocessdetails.string_value as attributeValue1, inprocessdetails.number_value as attributeValue2, "
            + "			tc.task_category_name taskCategoryName, "
            + "			kpi.id as kpiNameId, kpi.name as kpiName, "
            + "			pc.id as productId, pc.name as productName  "
            + "from task_group tg ,attribute_fields_inprocess_details inprocessdetails,  "
            + "attributes as att, in_process_data as inprocess,kpiname kpi, products_master as pc, "
            + "attributes_steps  astep,task_name tn, task_category tc "
            + "where tg.task=att.task_name_id and tg.taskcategory =att.task_category_id and att.id=inprocessdetails.attributes_fields_process_type_id "
            + "and inprocess.id=inprocessdetails.attributes_fields_in_process_id and inprocess.kpi_name_id=:kpiNameId "
            + "and astep.kpi_name_id=:kpiNameId and pc.id=astep.product_master_id and inprocess.kpi_name_id=kpi.id  "
            + "and astep.task_name_id=att.task_name_id and astep.task_category_id=att.task_category_id "
            + "and tn.id=astep.task_name_id and tc.id=astep.task_category_id", nativeQuery = true)
    List<Tuple> getAllDetails(@Param("kpiNameId") Long kpiNameId);

    @Query(value = "select q1.*, q2.* from (select DISTINCT ON (pat.id) pat.id as patId, pat.tgid as taskGroupId1, tg.taskcategory as taskCategoryId,task as taskNameId, "
            + "			tn.task_name taskName, astep.id as stepId, astep.step_number as stepNumber, "
            + "			pat.atid as attributeId, att.name as attributeName, "
            + "			pat.resval as attributeValue, pat.shift_date as inwardDate,"
            + "			tc.task_category_name taskCategoryName, kpi.id as kpiNameId, kpi.name as kpiName,"
            + "			pc.id as productId, pc.name as productName "
            + "			 from attributes_steps  astep,task_group tg, task_name tn, task_category tc, kpiname kpi, products_master as pc,"
            + "			 in_process_data as pat, attributes as att" + "			 where pat.kpi_name_id=:kpiNameId AND "
            + " 		pat.batchno=:batchNo "
            + " AND (cast(:inwardDate as timestamp) is null or (date(pat.shift_date) = date(cast(:inwardDate as timestamp)))) "
            + "			 and astep.kpi_name_id=:kpiNameId and pc.id=astep.product_master_id and pat.kpi_name_id=kpi.id "
            + "			and pat.atid=att.id "
            + "			 and pat.tgid=(tg.taskgroup) and tn.id=tg.task and tc.id=tg.taskcategory) as q1 left join "
            + "			(select DISTINCT ON (taskgroup) taskgroup taskGroupId, task_group_name as taskGroupName from task_group ) as q2"
            + "			on q1.taskGroupId1=q2.taskGroupId" + "			  ", nativeQuery = true)
    List<Tuple> getAllInProcessDetails(@Param("kpiNameId") Long kpiNameId, @Param("inwardDate") String inwardDate,
                                       @Param("batchNo") String batchNo);

    @Query(value = " select p.id as proreportDbId, p.tgid as tgId,  a.id  as id , a.name as name , a.disp_order as positionNumber, "
            + " p.attribute_min_value as minvalue, p.attribute_max_value as maxvalue , p.attype as attributeType,  p.input_type as inputType, "
            + " p.formula as formula , p.units  as unit , ip.atid as attributesResId,  ip.resval as attributesResValue  from proreportdb p "
            + " left JOIN attributes a on a.id = p.atid left JOIN qc_inprocess ip on ip.tgid = p.tgid AND ip.atid=a.id AND (:batchNo is null or ip.batchno=:batchNo ) "
            + " where  p.atid > 0 and p.kpi=:kpiNameId and p.refid=:productId  and p.tgid=:tgId  ORDER BY a.disp_order ASC ", nativeQuery = true)
    List<AttributesList> getAttributesAndValues(@Param(value = "tgId") Long tgId,
                                                @Param(value = "kpiNameId") Long kpiNameId, @Param(value = "productId") Long productId,
                                                @Param(value = "batchNo") String batchNo);

    @Query(value = " SELECT ip.batchno as batchno,  max(ip.id) as qcId,  ip.kpi as kpiNameId, ip.shiftdate as shiftdate ,   "
            + " max(kpi.name) as kpiName , ip.shiftid as shiftId , max(ip.status) as status FROM qc_inprocess ip "
            + " LEFT JOIN kpiname kpi on kpi.id=ip.kpi  "
            + " WHERE (:kpiNameId is null or kpi.id =:kpiNameId ) "
            + " AND (:batchNo is null or ip.batchno=:batchNo ) "
            + " AND (:status is null or (lower(ip.status) LIKE ('%' || lower(:status) || '%')) ) "
            + " AND (cast(:shiftDate as date) is null or (date(ip.shiftdate) = date(cast(:shiftDate as date))))  "
            + " GROUP BY ip.kpi , ip.batchno , ip.shiftdate , ip.shiftid  order by max(ip.id) desc", nativeQuery = true)
    Page<QcInProcessList> getInProcessList(@Param("kpiNameId") Long kpiNameId, @Param("batchNo") String batchNo,
                                           @Param("status") String status,
                                           @Param("shiftDate") Date shiftDate, Pageable pageable);

    @Query(value = " select max(ip.id) as id, ip.kpi as kpiId, max(kpi.name) as kpiName,  ip.shiftid as shiftId, max(s.name) as shiftName, "
            + " ip.shiftdate as shiftDate, ip.batchno as batchNo, ip.batchid as batchId,  ip.dept as deptId, max(dd.name) as deptName,  "
            + " max(ip.status) as status ,ip.quantity as quantity ,ip.batchbinno as batchBinNo,ip.batchproduct as batchProductNo ,pm.name as batchProduct FROM qc_inprocess ip left join kpiname kpi on kpi.id=ip.kpi  "
            + " left join pshifts s on s.id=ip.shiftid left join department_details dd on dd.id=ip.dept left join products_master pm on pm.id = ip.batchproduct"
            + " WHERE  (:kpiNameId is null or kpi.id =:kpiNameId ) "
            + " AND (:batchNo is null or ip.batchno=:batchNo ) " + " AND (:shiftId is null or ip.shiftid=:shiftId ) "
            + " AND (cast(:shiftDate as date) is null or (date(ip.shiftdate) = date(cast(:shiftDate as date))))  "
            + " GROUP BY ip.kpi, ip.shiftid , ip.shiftdate , ip.batchno , ip.batchid , ip.dept , ip.quantity,ip.batchbinno,ip.batchproduct,pm.name order by max(ip.id) asc", nativeQuery = true)
    QcInProcessDataList getQcInProcessDataList(@Param("kpiNameId") Long kpiNameId, @Param("batchNo") String batchNo,
                                               @Param("shiftId") Long shiftId, @Param("shiftDate") Date shiftDate);

    @Query(value = "select ip.kpi as kpiId, max(kpi.name) as kpiName,  ip.shiftid as shiftId, max(s.name) as shiftName, "
            + " ip.shiftdate as shiftDate, ip.batchno as batchNo, ip.batchid as batchId,  ip.dept as deptId, max(dd.name) as deptName,  "
            + " ip.mcid as machineId, max(m.name) as machineNme , ip.binname as binName, ip.binid as binId , ip.packingbatchno as packingBatchNo,ip.batchproduct as batchProductNo FROM qc_inprocess ip left join kpiname kpi on kpi.id=ip.kpi  "
            + " left join shifts s on s.id=ip.shiftid left join department_details dd on dd.id=ip.dept  left join machine m on m.id=ip.mcid "
            + " WHERE  (:kpiNameId is null or kpi.id =:kpiNameId ) "
            + " AND (:batchNo is null or ip.batchno=:batchNo ) "
            + " AND (:shiftId is null or ip.shiftid=:shiftId ) "
            + " AND (:tgId is null or ip.tgid=:tgId ) "
            + " AND (cast(:shiftDate as date) is null or (date(ip.shiftdate) = date(cast(:shiftDate as date))))  "
            + " GROUP BY ip.kpi, ip.shiftid , ip.shiftdate , ip.batchno , ip.batchid , ip.dept , ip.mcid ,  ip.binname , ip.binid , ip.tgid,ip.packingbatchno,ip.batchproduct  order by max(ip.id) asc", nativeQuery = true)
    List<QcInProcessDataList> getQcInProcessDataDetailList(@Param("kpiNameId") Long kpiNameId, @Param("tgId") Long tgId,
                                                           @Param("batchNo") String batchNo, @Param("shiftId") Long shiftId, @Param("shiftDate") Date shiftDate);

    @Query(value = "select p.id as proreportDbId, p.tgid as tgId,  a.id  as id , a.name as name , a.disp_order as positionNumber, "
            + " p.attribute_min_value as minvalue, p.attribute_max_value as maxvalue , p.attype as attributeType,  p.input_type as inputType, "
            + " p.formula as formula , p.units  as unit , ip.atid as attributesResId,  ip.resval as attributesResValue  from proreportdb p "
            + " left JOIN attributes a on a.id = p.atid left JOIN qc_inprocess ip on ip.tgid = p.tgid AND ip.atid=a.id AND (:batchNo is null or ip.batchno=:batchNo ) "
            + " AND (:shiftId is null or ip.shiftid=:shiftId ) AND (:machineId  is null or ip.mcid = :machineId)AND (:productId   is null or p.refid = :productId ) "
            + " AND (cast(:shiftDate as date) is null or (date(ip.shiftdate) = date(cast(:shiftDate as date))))  "
            + " AND (:binName is null or ip.binname=:binName ) " + " AND (:binId is null or ip.binid=:binId ) AND (:packingBatchNo is null or ip.packingbatchno= :packingBatchNo) "
            + " where  p.atid > 0 and p.kpi=:kpiNameId  and p.tgid=:tgId  "
            + "ORDER BY a.disp_order ASC ", nativeQuery = true)
    List<AttributesList> getAttributesAndValues(@Param(value = "tgId") Long tgId,
                                                @Param(value = "kpiNameId") Long kpiNameId, @Param(value = "productId") Long productId,
                                                @Param(value = "batchNo") String batchNo, @Param(value = "machineId") Long machineId,
                                                @Param(value = "shiftId") Long shiftId, @Param(value = "shiftDate") Date shiftDate,
                                                @Param(value = "binName") Long binName, @Param(value = "binId") String binId,
                                                @Param(value = "packingBatchNo") String packingBatchNo);

    @Transactional
    @Modifying
    @Query(value = "UPDATE qc_inprocess SET status='completed' WHERE  kpi=:kpiNameId AND batchno=:batchNo AND shiftid=:shiftId  AND (date(shiftdate) = date(cast(:shiftDate as date)))", nativeQuery = true)
    void updateStatus(@Param("kpiNameId") Long kpiNameId, @Param("batchNo") String batchNo,
                      @Param("shiftId") Long shiftId, @Param("shiftDate") Date shiftDate);


    @Query(value = "SELECT DISTINCT qi.tno as trnNo, :transactionType as trnName, " +
            "           qi.branch as issueBranchId, qi.dept as issueDeptId, " +
            "            qi.refid as refId, pm.name as productName, qi.shiftdate as trnDate  " +
            "            FROM qc_inprocess qi " +
            "          LEFT JOIN products_master pm ON qi.refid = pm.id " +
            "            WHERE qi.shiftdate = :shiftDate " +
            "            AND qi.branch = :branch AND qi.dept = :dept AND qi.tno IS NOT NULL", nativeQuery = true)
    List<TransactionList> getTransactionListFilterByQCInProcess(@Param("shiftDate") Date shiftDate, @Param("transactionType") String transactionType,
                                                                @Param("branch") Long branch, @Param("dept") Long dept);


    @Query(value = "select * FROM qc_inprocess ip  WHERE  " +
            " (:kpiNameId is null or ip.kpi =:kpiNameId ) " +
            " AND (:tgid is null or ip.tgid=:tgid )  " +
            " AND (:batchNo is null or ip.batchno=:batchNo )  " +
            " AND (:shiftId is null or ip.shiftid=:shiftId ) " +
            " AND (cast(:shiftDate as date) is null or (date(ip.shiftdate) = date(cast(:shiftDate as date))))  limit 1  ", nativeQuery = true)
    Optional<QcInProcess> findQcInProcess( @Param("tgid") Long tgid,
                                          @Param("batchNo") String batchNo,
                                          @Param("kpiNameId") Long kpiNameId,
                                          @Param("shiftId") Long shiftId,
                                          @Param("shiftDate") Date shiftDate);

    @Query(value = " select ip.kpi as kpiNameId , k.name as kpiName , max(k.formno) as isoNo , max(br.name) as plant, ip.shiftid  as shiftId, " +
            " max(p.name)  as shiftName , ip.shiftdate as shiftDate , max(ed.employeename) as  testedBy " +
            " FROM qc_inprocess ip  left join kpiname k on k.id = ip.kpi " +
            " left join branch br on br.id = ip.branch left join pshifts p on p.id = ip.shiftid " +
            " left join employee_details ed on ed.id = ip.created_by" +
            " WHERE  (:kpiNameId is null or ip.kpi =:kpiNameId ) " +
            " AND (:batchNo is null or ip.batchno=:batchNo )  " +
            " AND (:shiftId is null or ip.shiftid=:shiftId ) " +
            " AND (cast(:shiftDate as date) is null OR (date(ip.shiftdate) = date(cast(:shiftDate as date))))" +
            " group by ip.kpi , k.name , ip.branch , ip.batchno , ip.shiftid , ip.shiftdate" +
            " order by max(ip.id) desc  limit 1 ", nativeQuery = true)
    QcInProcessEntry findQcInProcessEntryResponse(@Param("batchNo") String batchNo,
                                          @Param("kpiNameId") Long kpiNameId,
                                          @Param("shiftId") Long shiftId,
                                          @Param("shiftDate") Date shiftDate);




    @Query(value = "select ip.mcid  as machineId , max(m.name) as machineName  ,ip.packingbatchno as packingBatchNo , ip.binname as binName" +
            " FROM qc_inprocess ip  " +
            " left join machine m on m.id = ip.mcid " +
            " WHERE  (:kpiNameId is null or ip.kpi =:kpiNameId ) " +
            " AND (:batchNo is null or ip.batchno=:batchNo )  " +
            " AND (:shiftId is null or ip.shiftid=:shiftId ) " +
            " AND (cast(:shiftDate as date) is null OR (date(ip.shiftdate) = date(cast(:shiftDate as date))))" +
            " group by ip.kpi , ip.mcid, ip.batchno , ip.shiftid , ip.shiftdate ,ip.packingbatchno ,ip.binname " +
            " order by max(ip.id) desc ", nativeQuery = true)
    List<QcInProcessEntry> findQcInProcessEntryMachineList(@Param("batchNo") String batchNo,
                                                  @Param("kpiNameId") Long kpiNameId,
                                                  @Param("shiftId") Long shiftId,
                                                  @Param("shiftDate") Date shiftDate);



    @Query(value = " select  a.id  as attributeId , a.name as attributeName , " +
            " p.tgid as tgid , a.disp_order as positionNumber from proreportdb p " +
            " left JOIN attributes a on a.id = p.atid left join kpi k on k.tgid =p.tgid and k.id=p.kpi  " +
            " where  p.atid > 0 and p.kpi=:kpiNameId  " +
            " ORDER BY k.dispord  ,a.disp_order asc", nativeQuery = true)
    List<QcInProcessEntryAttributes> findQcInProcessEntryAttributesList(@Param("kpiNameId") Long kpiNameId);


    @Query(value = "select * FROM qc_inprocess ip  WHERE  " +
            " (:kpiNameId is null or ip.kpi =:kpiNameId ) " +
            " AND (:tgid is null or ip.tgid=:tgid )  " +
            " AND (:batchNo is null or ip.batchno=:batchNo )  " +
            " AND (:shiftId is null or ip.shiftid=:shiftId ) " +
            " AND (:mcid is null or ip.mcid=:mcid ) " +
            " AND (:attId is null or ip.atid=:attId ) " +
            " AND (cast(:shiftDate as date) is null or (date(ip.shiftdate) = date(cast(:shiftDate as date)))) AND (:packingBatcNo is null or ip.packingbatchno=:packingBatcNo )AND (:binName is null or ip.binname=:binName ) limit 1  ", nativeQuery = true)
    Optional<QcInProcess> findQcInProcess( @Param("tgid") Long tgid,
                                           @Param("batchNo") String batchNo,
                                           @Param("kpiNameId") Long kpiNameId,
                                           @Param("shiftId") Long shiftId,
                                           @Param("shiftDate") Date shiftDate,
                                           @Param("mcid") Long mcid,
                                           @Param("attId") Long attId,
                                           @Param("packingBatcNo") String packingBatcNo,
                                           @Param("binName") Long binName);
    

}
