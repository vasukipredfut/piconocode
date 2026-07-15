package com.aria.dao;

import java.util.Date;
import java.util.List;
import jakarta.persistence.Tuple;

import com.aria.dto.AttributesList;
import com.aria.dto.FormulaAttributesList;
import com.aria.dto.MaintenanceAttributesList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Proreportdb;

@Repository
public interface ProreportdbRepository extends JpaRepository<Proreportdb, Long> {


    @Query(value = "select q1.*, q2.* from (select DISTINCT ON (pat.id) pat.id as patId, pat.tgid as taskGroupId1, tg.taskcategory as taskCategoryId,task as taskNameId, "
            + "			tn.task_name taskName, astep.id as stepId, astep.step_number as stepNumber, "
            + "			pat.atid as attributeId,att.name as attributeName, "
            + "			pat.resval as attributeValue, pat.entry as inwardDate,"
            + "			tc.task_category_name taskCategoryName, kpi.id as kpiNameId, kpi.name as kpiName,"
            + "			pc.id as productId, pc.name as productName "
            + "			 from attributes_steps  astep,task_group tg, task_name tn, task_category tc, kpiname kpi, products_master as pc,"
            + "			 patlog as pat, attributes as att"
            + "			 where pat.kpi=:kpiNameId AND "
//			+ "		pat.batchno= " + "'" + "Prdn-VVV/P1/PRODN/A-5966-777-150-743-mac " + "'"
            + " 		pat.batchno=:batchNo "
            + " AND (cast(:inwardDate as timestamp) is null or (date(pat.entry) = date(cast(:inwardDate as timestamp)))) "
//			+ "			 AND date(pat.entry) = " + "'" + "2021-01-01 " + "'"
            + "			 and astep.kpi_name_id=:kpiNameId and pc.id=astep.product_master_id and pat.kpi=kpi.id "
            + "			and pat.atid=att.id "
            + "			 and pat.tgid=(tg.taskgroup) and tn.id=tg.task and tc.id=tg.taskcategory) as q1 left join "
            + "			(select DISTINCT ON (taskgroup) taskgroup taskGroupId, task_group_name as taskGroupName from task_group ) as q2"
            + "			on q1.taskGroupId1=q2.taskGroupId"
            + "			  ", nativeQuery = true)
    List<Tuple> getAllDetails(@Param("kpiNameId") Long kpiNameId,
                              @Param("inwardDate") String inwardDate,
                              @Param("batchNo") String batchNo);





    @Query(value = "select p.id as proreportDbId, tg.id as tgId,  a.id  as id , a.name as name , a.disp_order as positionNumber, p.attribute_min_value as minvalue, "
            + " p.attribute_max_value as maxvalue , p.attype as attributeType, p.input_type as inputType, p.formula as formula , p.units  as unit  from  task_group tg "
            + " left join proreportdb p on p.tgid  = tg.id left join attributes a on a.id = p.atid "
            + " where tg.id =:tgId and p.atid > 0 and p.kpi=:kpiNameId and p.refid=:productId  ORDER BY p.id ASC ", nativeQuery = true)
    List<AttributesList> getAttributesFields(@Param(value = "tgId") Long tgId,
                                             @Param(value = "kpiNameId") Long kpiNameId,
                                             @Param(value = "productId") Long productId);

    @Query(value = "select pr.* from proreportdb pr LEFT JOIN attributes a ON a.id = pr.atid where pr.kpi=:kpiNameId and pr.refid=:productId"
            + " and pr.tgid=:taskGroupId and pr.tcat=:taskCategoryId and pr.atid > 0  ORDER BY a.disp_order ASC", nativeQuery = true)
    List<Proreportdb> getAttributesFields1(@Param(value = "taskGroupId") Long taskGroupId,
                                           @Param(value = "taskCategoryId") Long taskCategoryId,
                                           @Param(value = "kpiNameId") Long kpiNameId,
                                           @Param(value = "productId") Long productId);


    @Query(value = " select p2.atid  as id, max(p2.id ) as proreportDbId ,  p2.atname  as name , p2.attype  as attributeType , max(k.dispord) as disp , max(p2.attribute_min_value) as minvalue,  max(p2.attribute_max_value) as maxValue, max(p2.formula) as formula, max(p3.resid) as attributesResId , max(p3.resval)  as attributesResValue  " +
            "             from kpi k left join  proreportdb p2 on p2.tgid = k.tgid and p2.kpi = k.id and p2.refid = k.refid left join patlog p3 on p3.tgid =p2.tgid and p2.kpi = p3.kpi and p2.atid  = p3.atid  and (:entryType is null or p3.tno =:entryType) and (:batchNo is null or p3.batchno =:batchNo)" +
            "             where  p2.atid > 0 and k.id =:kpiNameId group by p2.atid , p2.atname , p2.attype order by  disp ", nativeQuery = true)
    List<AttributesList> getAttributesFieldList(@Param(value = "kpiNameId") Long kpiNameId,
                                                @Param(value = "batchNo") String batchNo,
                                                @Param(value = "entryType") String entryType);


    @Query(value = "select pr.* from proreportdb pr where pr.kpi=:kpiNameId and pr.refid=:productId"
            + " and pr.tgid=:tgId and pr.atid > 0  ", nativeQuery = true)
    List<Proreportdb> findAttributesFields(@Param(value = "tgId") Long tgId,
                                           @Param(value = "kpiNameId") Long kpiNameId,
                                           @Param(value = "productId") Long productId);


    @Query(value = "select p.id as proreportDbId,  tg.id as tgId,  a.id  as id , a.name as name , a.disp_order as positionNumber, p.attribute_min_value as minvalue, a.purpose_benefit as purposeBenefit ,  "
            + "     a.workinst_saftmeasure as workinstSaftmeasure ,   p.attribute_max_value as maxvalue , p.attype as attributeType, p.input_type as inputType  from  task_group tg "
            + "           left join task_category tc on tc.id = tg.taskcategory and tc.inw_ins_flag ='0' and tc.inp_ins_flag ='0' left join proreportdb p on p.tgid  = tg.id left join attributes a on a.id = p.atid "
            + "             where tg.id =:tgId and  p.atid > 0  ORDER BY a.disp_order ASC", nativeQuery = true)
    List<MaintenanceAttributesList> getMaintenanceAttributesFields(@Param(value = "tgId") Long tgId);


    @Query(value = "select p.id as proreportDbId,  tg.id as tgId,  a.id  as id , a.name as name , a.disp_order as positionNumber, p.attribute_min_value as minvalue, a.purpose_benefit as purposeBenefit ,  "
            + "     a.workinst_saftmeasure as workinstSaftmeasure ,   p.attribute_max_value as maxvalue , p.attype as attributeType, p.input_type as inputType  from  task_group tg "
            + "           left join task_category tc on tc.id = tg.taskcategory and tc.inw_ins_flag ='0' and tc.inp_ins_flag ='0' left join proreportdb p on p.tgid  = tg.id left join attributes a on a.id = p.atid "
            + "             where tg.task =:taskId and tg.taskcategory =:taskcategoryId and tg.taskgroup =:taskgroupId and p.atid > 0  ORDER BY a.disp_order ASC", nativeQuery = true)
    List<MaintenanceAttributesList> getMaintenanceAttributesFieldList(@Param(value = "taskId") Long taskId,
                                                                      @Param(value = "taskcategoryId") Long taskcategoryId,
                                                                      @Param(value = "taskgroupId") Long taskgroupId);




    @Query(value = " SELECT p.id as id,  p.atid as attId , p.atname as attName, p.attribute_min_value as minvalue , p.attribute_max_value as maxvalue ,  p.input_type as inputType, p.formula as formula "
            + " FROM proreportdb p where p.atid > 0 "
            + " AND (:id is null or p.id =:id )"
            + " AND (:tgId is null or p.tgid =:tgId )"
            + " AND (:attName is null or (lower(p.atname) LIKE ('%' || lower(:attName) || '%'))) "
            + " AND (:kpiNameId is null or p.kpi =:kpiNameId )" , nativeQuery = true)
    List<FormulaAttributesList> formulaAttributesList(@Param(value = "kpiNameId") Long kpiNameId,
                                                      @Param(value = "id") Long id,
                                                      @Param(value = "tgId") Long tgId,
                                                       @Param(value = "attName") String attName);


    @Query(value = " SELECT p2.atid AS id,MAX(p2.id) AS proreportDbId,p2.atname AS name,p2.attype AS attributeType,MAX(k.dispord) AS disp,"
   + "  MAX(p2.attribute_min_value) AS minvalue,MAX(p2.attribute_max_value) AS maxValue,MAX(p2.formula) AS formula,p3.resid AS attributesResId,"
   + " p3.resval AS attributesResValue,p2.units AS unit,p3.mcid AS machineId, p3.packingbatchno  AS packingBatchNo , p3.binname AS binName from kpi k LEFT JOIN  "
   + "  proreportdb p2 ON p2.tgid = k.tgid AND p2.kpi = k.id AND p2.refid = k.refid LEFT JOIN qc_inprocess p3 ON p3.tgid = p2.tgid "
   + " AND p3.kpi = p2.kpi AND p3.atid = p2.atid AND (:batchNo IS NULL OR p3.batchno = :batchNo)"
   + "  AND (:shiftId IS NULL OR p3.shiftid = :shiftId) AND(cast(:shiftDateCon as date) is null or (date(p3.shiftdate) = date(cast(:shiftDateCon as date)))) WHERE  p2.atid > 0 AND k.id = :kpiNameId "
   + " GROUP BY p2.atid, p2.atname, p2.attype,p3.resid ,p3.resval ,p2.units,p3.mcid,p3.packingbatchno,p3.binname ORDER BY disp ", nativeQuery = true)
    List<AttributesList> getAllAttributeListForQc(@Param(value = "kpiNameId") Long kpiNameId,@Param(value = "batchNo") String batchNo,@Param(value = "shiftId") Long shiftId,@Param(value = "shiftDateCon") Date shiftDateCon);
   

}
