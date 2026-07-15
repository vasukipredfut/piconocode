package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.AttributesList;
import com.aria.dto.AttributesNameValueList;
import com.aria.dto.PatlogList;
import com.aria.model.Patlog;

@Repository
public interface PatlogRepository extends JpaRepository<Patlog, Long> {

	/*
	 * 
	 * @Query(value =
	 * "select q1.*, q2.* from (select DISTINCT ON (p.id)  p.id as patId, tg.id as taskGroupId1, tc.id as taskCategoryId, tn.id as taskNameId, "
	 * +
	 * " tn.task_name taskName,  att.id as attributeId, att.name as attributeName, p.resval as attributeValue, p.entry as inwardDate, "
	 * +
	 * " tc.task_category_name taskCategoryName, kn.id as kpiNameId, kn.name as kpiName, pc.id as productId, pc.name as productName "
	 * +
	 * " from   patlog p LEFT JOIN task_group tg ON tg.taskgroup = p.tgid LEFT JOIN task_name tn ON tn.id = tg.task "
	 * +
	 * " LEFT JOIN task_category tc ON tc.id = tg.taskcategory LEFT JOIN kpiname kn ON kn.id = p.kpi LEFT JOIN  products_master pc ON pc.id = p.id "
	 * +
	 * " LEFT JOIN attributes att ON att.id = p.atid  where p.kpi = kn.id AND p.kpi =:kpiNameId "
	 * + " AND p.batchno=:batchNo " +
	 * " AND (cast(:inwardDate as date) is null or (date(p.entry) = date(cast(:inwardDate as date)))) ) as q1 "
	 * +
	 * " left join (select DISTINCT ON (taskgroup) taskgroup as taskGroupId, task_group_name as taskGroupName from task_group ) as q2 on q1.taskGroupId1=q2.taskGroupId "
	 * , nativeQuery = true) List<Tuple> getByAllDetails(@Param("kpiNameId") Long
	 * kpiNameId, @Param("inwardDate") Date inwardDate, @Param("batchNo") String
	 * batchNo);
	 * 
	 * 
	 * @Query(value =
	 * "select q1.*, q2.* from (select DISTINCT ON (p.id)  p.id as patId, tg.id as taskGroupId1, tc.id as taskCategoryId, tn.id as taskNameId, "
	 * +
	 * " tn.task_name taskName,  att.id as attributeId, att.name as attributeName, p.resval as attributeValue, p.entry as inwardDate, p.skipflag as skipFlag, "
	 * +
	 * " tc.task_category_name taskCategoryName, kn.id as kpiNameId, kn.name as kpiName, pc.id as productId, pc.name as productName "
	 * +
	 * " from   patlog p LEFT JOIN task_group tg ON tg.taskgroup = p.tgid LEFT JOIN task_name tn ON tn.id = tg.task "
	 * +
	 * " LEFT JOIN task_category tc ON tc.id = tg.taskcategory LEFT JOIN kpiname kn ON kn.id = p.kpi LEFT JOIN  products_master pc ON pc.id = p.id "
	 * +
	 * " LEFT JOIN attributes att ON att.id = p.atid  WHERE p.kpi = kn.id AND p.kpi =:kpiNameId "
	 * + " AND p.batchno=:batchNo " + " AND p.tno=:entryType " +
	 * " AND (cast(:inwardDate as date) is null or (date(p.entry) = date(cast(:inwardDate as date))))  "
	 * +
	 * " AND tc.id =:taskcategory AND tn.id =:task AND tg.taskgroup=:taskgroup) as q1 "
	 * +
	 * " left join (select DISTINCT ON (taskgroup) taskgroup as taskGroupId, task_group_name as taskGroupName from task_group ) as q2 on q1.taskGroupId1=q2.taskGroupId "
	 * , nativeQuery = true) List<Tuple> getByAllDetails(@Param("entryType") String
	 * entryType, @Param("kpiNameId") Long kpiNameId, @Param("task") Long
	 * task, @Param("taskcategory") Long taskcategory, @Param("taskgroup") Long
	 * taskgroup, @Param("inwardDate") Date inwardDate, @Param("batchNo") String
	 * batchNo);
	 * 
	 */

	@Query(value = "select ia.id as id, pat.batchno as batchno, pat.tno as entryType, max(pat.no_of_bag) as noOfBag, max(pat.id) as patId,  max(pat.kpi) as kpiNameId, max(pat.entry) as inwardDate, max(ia.status) as status, max(ia.batch_type) as statusType, "
			+ " max(kpi.name) as kpiName FROM patlog pat left join kpiname kpi on kpi.id=pat.kpi left join inward_audit ia on ia.batch_no = pat.batchno "
			+ " WHERE (ia.entry_type  is null or ia.entry_type = pat.tno ) AND (:kpiNameId is null or kpi.id =:kpiNameId ) "
			+ " AND (:batchNo is null or pat.batchno=:batchNo ) " + " AND (:entryType is null or pat.tno=:entryType ) "
			+ " AND (:status is null or ia.status=:status )  "
			+ " AND (cast(:inwardDate as date) is null or (date(pat.entry) = date(cast(:inwardDate as date))))  "
	        + " AND (kpi.type = 'I')  "
			+ " GROUP BY batchno , ia.id, tno order by max(pat.id) desc", nativeQuery = true)
	Page<PatlogList> getInspectionDetailsList(@Param("kpiNameId") Long kpiNameId, @Param("entryType") String entryType,
			@Param("batchNo") String batchNo, @Param("inwardDate") Date inwardDate, @Param("status") String status,
			Pageable pageable);

	/*
	 * 
	 * @Query(value =
	 * "select * from patlog pat where pat.kpi =:kpiNameId AND pat.atid =:attId order by id desc Limit 1"
	 * , nativeQuery = true) Optional<Patlog>
	 * getInspectionDetail(@Param("kpiNameId") Long kpiNameId, @Param("attId") Long
	 * attId);
	 */

	@Query(value = "select p.id as proreportDbId, p.tgid as tgId,  a.id  as id , a.name as name , a.disp_order as positionNumber, "
			+ " p.attribute_min_value as minvalue, p.attribute_max_value as maxvalue , p.attype as attributeType,  p.input_type as inputType, "
			+ " p.formula as formula , p.units  as unit , "
			+ " pat.resid as attributesResId,  pat.resval as attributesResValue ,  pat.skipflag as skipFlag from "
			+ " proreportdb p  left JOIN attributes a on a.id = p.atid left JOIN "
			+ " patlog pat on pat.tgid = p.tgid AND pat.atid=a.id AND (:batchNo is null or pat.batchno=:batchNo ) AND (:entryType is null or pat.tno=:entryType )"
			+ " where  p.atid > 0 and p.kpi=:kpiNameId and p.refid=:productId  and p.tgid=:tgId  ORDER BY p.id ASC ", nativeQuery = true)
	List<AttributesList> getAttributesAndValues(@Param(value = "tgId") Long tgId,
			@Param(value = "kpiNameId") Long kpiNameId, @Param(value = "productId") Long productId,
			@Param(value = "batchNo") String batchNo, @Param(value = "entryType") String entryType);

	@Query(value = "SELECT name, attributesResValue " + "FROM ("
			+ "SELECT a.name AS name, pat.resval AS attributesResValue, "
			+ "k.dispord AS k_dispord, a.disp_order AS a_disp_order, "
			+ "ROW_NUMBER() OVER (PARTITION BY a.name, pat.resval ORDER BY k.dispord ASC, a.disp_order ASC) AS rn "
			+ "FROM PatLog pat " + "LEFT JOIN attributes a ON pat.atid = a.id "
			+ "LEFT JOIN kpi k ON pat.tgid = k.tgid " + "WHERE (:batchNo IS NULL OR pat.batchno = :batchNo)"
			+ "AND (:entryType IS NULL OR pat.tno = :entryType) " + ") AS subquery " + "WHERE rn = 1 "
			+ "ORDER BY k_dispord ASC, a_disp_order ASC", nativeQuery = true)
	List<AttributesNameValueList> getAttributesAndValues(@Param(value = "batchNo") String batchNo,
			@Param(value = "entryType") String entryType);

	
	

	

}
