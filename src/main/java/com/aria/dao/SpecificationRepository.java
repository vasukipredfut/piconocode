package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Specification;
import com.aria.model.SpecificationDetails;

@Repository
public interface SpecificationRepository extends JpaRepository<Specification, Long> {

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, pm.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, u.employeename as createdBy FROM specification s, products_master pm ,employee_details u WHERE pm.id=s.refid and s.specification_type =28 and u.id=s.created_by", nativeQuery = true)
    Page<SpecificationDetails> ListSpecificationforProduct(@Param("orgid") Long orgid, Pageable pageable);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, pm.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, u.employeename as createdBy FROM specification s, products_master pm , employee_details u WHERE pm.id=s.refid and s.specification_type =28 and u.id=s.created_by and (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) and (cast(:toDt as timestamp) is null or s.created_date <= :toDt) AND (:productName is null or lower(pm.name) like ('%' || lower(:productName) || '%')) order by s.id desc ", nativeQuery = true)
    Page<SpecificationDetails> ListSpecificationforProduct(@Param("productName") String productName, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt, Pageable pageable);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, m.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, u.employeename as createdBy  FROM specification s, machine m,employee_details u WHERE m.id=s.refid and s.specification_type =29 and u.id=s.created_by and (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) and (cast(:toDt as timestamp) is null or s.created_date <= :toDt) AND (:machineName is null or lower(m.name) like ('%' || lower(:machineName) || '%')) order by s.id desc ", nativeQuery = true)
    Page<SpecificationDetails> ListSpecificationforMachine(@Param("machineName") String machineName, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt, Pageable pageable);


    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, e.amc_expense_name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, u.employeename as createdBy  FROM specification s, purchase_amc_expense e,employee_details u WHERE e.amc_id=s.refid and s.specification_type =30 and u.id=s.created_by and (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) and (cast(:toDt as timestamp) is null or s.created_date <= :toDt) AND (:expenseName is null or lower(e.amc_expense_name) like ('%' || lower(:expenseName) || '%')) order by s.id desc ", nativeQuery = true)
    Page<SpecificationDetails> ListSpecificationforExpense(@Param("expenseName") String expenseName, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt, Pageable pageable);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, e.amc_expense_name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, u.employeename as createdBy  FROM specification s, purchase_amc_expense e,employee_details u WHERE e.amc_id=s.refid and s.specification_type =31 and u.id=s.created_by and (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) and (cast(:toDt as timestamp) is null or s.created_date <= :toDt) AND (:expenseName is null or lower(e.amc_expense_name) like ('%' || lower(:expenseName) || '%')) order by s.id desc ", nativeQuery = true)
    Page<SpecificationDetails> ListSpecificationforAMCExpense(@Param("expenseName") String expenseName, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt, Pageable pageable);


    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, pm.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy FROM specification s, products_master pm WHERE pm.id=s.refid and s.specification_type =28 and ( :name is null or pm.name = :name) and (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) and (cast(:toDt as timestamp) is null or s.created_date <= :toDt) ", nativeQuery = true)
    List<SpecificationDetails> ListSpecificationforProduct(@Param("name") String name, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, m.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, machine m WHERE m.id=s.refid and s.specification_type =29 and ( :name is null or m.name = :name) and (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) and (cast(:toDt as timestamp) is null or s.created_date <= :toDt)", nativeQuery = true)
    List<SpecificationDetails> ListSpecificationforMachine(@Param("name") String name, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt);


    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, e.amc_expense_name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, purchase_amc_expense e WHERE e.amc_id=s.refid and s.specification_type =30 and ( :name is null or e.amc_expense_name = :name) and (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) and (cast(:toDt as timestamp) is null or s.created_date <= :toDt)", nativeQuery = true)
    List<SpecificationDetails> ListSpecificationforExpense(@Param("name") String name, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, e.amc_expense_name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, purchase_amc_expense e WHERE e.amc_id=s.refid and s.specification_type =31 and ( :name is null or e.amc_expense_name = :name) and (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) and (cast(:toDt as timestamp) is null or s.created_date <= :toDt)", nativeQuery = true)
    List<SpecificationDetails> ListSpecificationforAMCExpense(@Param("name") String name, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt);


    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, pm.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy FROM specification s, products_master pm WHERE pm.id=s.refid and s.specification_type =28 and s.id =:id", nativeQuery = true)
    SpecificationDetails findbyProductSpecificationId(@Param("id") Long id);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, pm.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy FROM specification s, products_master pm WHERE pm.id=s.refid and s.specification_type =28 and s.refid=:prodId LIMIT 1", nativeQuery = true)
    SpecificationDetails findbyProductSpecificationIdAndProdId(@Param("prodId") Long prodId);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, m.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, machine m WHERE m.id=s.refid and s.specification_type =29 and s.id =:id", nativeQuery = true)
    SpecificationDetails findbyMachineSpecificationId(@Param("id") Long id);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, m.name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, machine m WHERE m.id=s.refid and s.refid=:prodId and s.specification_type =29", nativeQuery = true)
    SpecificationDetails findbyMachineSpecificationIdAndProdId(@Param("prodId") Long prodId);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, e.amc_expense_name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, purchase_amc_expense e WHERE e.amc_id=s.refid and s.specification_type =30 and s.id =:id", nativeQuery = true)
    SpecificationDetails findbyExpenseSpecificationId(@Param("id") Long id);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, e.amc_expense_name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, purchase_amc_expense e WHERE e.amc_id=s.refid and s.refid=:prodId and s.specification_type =30", nativeQuery = true)
    SpecificationDetails findbyExpenseSpecificationIdAndProdId(@Param("prodId") Long prodId);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, e.amc_expense_name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, purchase_amc_expense e WHERE e.amc_id=s.refid and s.specification_type =31 and s.id =:id", nativeQuery = true)
    SpecificationDetails findbyAMCExpenseSpecificationId(@Param("id") Long id);

    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, e.amc_expense_name as refname, s.specification_type as spectype, s.branch as branch, s.department as department,s.organisation as org, s.created_date as createdDate, s.created_by as createdBy  FROM specification s, purchase_amc_expense e WHERE e.amc_id=s.refid and s.specification_type =31 and s.refid=:prodId ", nativeQuery = true)
    SpecificationDetails findbyAMCExpenseSpecificationIdAndProdId(@Param("prodId") Long prodId);

    @Query(value = "SELECT * FROM specification s WHERE s.organisation =:orgid and s.refid =:refId and s.specification_type =:specType ", nativeQuery = true)
    List<Specification> ListSpecification(@Param("orgid") Long orgid, @Param("refId") Long refId, @Param("specType") Long specType);


    @Query(value = "SELECT s.id as id, s.name as name, s.refid as refid, pm.name as refname, " +
            " s.specification_type as spectype, s.branch as branch, s.department as department,  s.organisation as org ," +
			" s.created_date as createdDate, s.created_by as createdBy , pm.standing_order as standingOrder, pm.standing_authorise as standingOrderAuth " +
            " FROM specification s, products_master pm WHERE pm.id=s.refid and s.specification_type = 28 " +
            " AND ( :name is null or pm.name = :name) AND (:standingOrder is null OR lower(pm.standing_order) LIKE ('%' || lower(:standingOrder) || '%')) " +
			" AND ( :standingOrderAuth is null or pm.standing_authorise = :standingOrderAuth) " +
			" AND (cast(:fromDt as timestamp) is null or s.created_date >= :fromDt) " +
            " AND (cast(:toDt as timestamp) is null or s.created_date <= :toDt) ", nativeQuery = true)
    List<SpecificationDetails> ListSpecificationForProductList(@Param("name") String name,
                                                               @Param("standingOrder") String standingOrder,
                                                               @Param("standingOrderAuth") String standingOrderAuth,
                                                               @Param("fromDt") Date fromDt,
                                                               @Param("toDt") Date toDt);

}
