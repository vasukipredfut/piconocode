package com.aria.dao;


import com.aria.dto.SalaryStructureList;
import com.aria.model.SalaryStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryStructureRepo extends JpaRepository<SalaryStructure,Long> {


    @Query(value = "SELECT *  FROM salary_structure s  WHERE  s.trnyear =:trnYear ", nativeQuery = true)
    List<SalaryStructure> findBy(@Param("trnYear") Long trnYear);

    @Query(value = "SELECT *  FROM salary_structure s  WHERE s.id=:id AND s.trnyear =:trnYear order by s.aid desc LIMIT 1", nativeQuery = true)
    Optional<SalaryStructure> findBy(@Param("id") Long id, @Param("trnYear") Long trnYear);

    @Query(value = "SELECT *  FROM salary_structure s  WHERE s.id=104 AND s.status ='A' ", nativeQuery = true)
    Optional<SalaryStructure> findByDefult();


    @Query(value = "select sl.id as desiId  ,sl.name as designation , sl.status as status,  sl.trnyear as year , sl.applyto as applyTo , sl.fixed as salaryType,"
            + " sl.minbasic as minimumBasic , sl.da as da, sl.allowance as allowance , sl.minhra as hra , sl.dacategory as dacatId, "
            + " d.name as category , sl.bper as basicDaPercentage, sl.gwper as goodWorkPercentage , sl.convper as conveyancePercentage,  "
            + " sl.maxconv as maximumConveyance , sl.minconv as minimumConveyance , sl.minhra as minimumHRA  from salary_structure sl "
            + " left join dacategory d on d.id = sl.dacategory   "
            + " WHERE (:trnYear is null OR sl.trnyear =:trnYear) "
            + " AND  (:status is null or (lower(sl.status) LIKE ('%' || lower(:status) || '%')) ) "
            + " AND  (:designation is null or (lower(sl.name) LIKE ('%' || lower(:designation) || '%')) ) "
            + " AND  (:salaryType is null or (lower(sl.fixed) LIKE ('%' || lower(:salaryType) || '%')) ) "
            + " AND  (:applyTo is null or (lower(sl.applyto) LIKE ('%' || lower(:applyTo) || '%')) ) ", nativeQuery = true)
    Page<SalaryStructureList> findByFilterList(@Param("trnYear") Long trnYear,
                                               @Param("status") String status,
                                               @Param("designation") String designation,
                                               @Param("salaryType") String salaryType,
                                               @Param("applyTo") String applyTo, Pageable pageable);


    @Query(value = "select sl.id as desiId  ,sl.name as designation , sl.status as status,  sl.trnyear as year , sl.applyto as applyTo , sl.fixed as salaryType,"
            + " sl.minbasic as minimumBasic , sl.da as da, sl.allowance as allowance , sl.minhra as hra , sl.dacategory as dacatId, "
            + " d.name as category , sl.bper as basicDaPercentage, sl.gwper as goodWorkPercentage , sl.convper as conveyancePercentage,  "
            + " sl.maxconv as maximumConveyance , sl.minconv as minimumConveyance , sl.minhra as minimumHRA  from salary_structure sl "
            + " left join dacategory d on d.id = sl.dacategory   "
            + " WHERE (:trnYear is null OR sl.trnyear =:trnYear) "
            + " AND  (:status is null or (lower(sl.status) LIKE ('%' || lower(:status) || '%')) ) "
            + " AND  (:designation is null or (lower(sl.name) LIKE ('%' || lower(:designation) || '%')) ) "
            + " AND  (:salaryType is null or (lower(sl.fixed) LIKE ('%' || lower(:salaryType) || '%')) ) "
            + " AND  (:applyTo is null or (lower(sl.applyto) LIKE ('%' || lower(:applyTo) || '%')) ) ", nativeQuery = true)
    List<SalaryStructureList> findByFilterList(@Param("trnYear") Long trnYear,
                                               @Param("status") String status,
                                               @Param("designation") String designation,
                                               @Param("salaryType") String salaryType,
                                               @Param("applyTo") String applyTo);



    @Query(value = "SELECT * FROM salary_structure sl "
            + " WHERE (:trnYear is null OR sl.trnyear =:trnYear) "
            + " AND  (:status is null or (lower(sl.status) LIKE ('%' || lower(:status) || '%')) ) "
            + " AND  (:designationId is null or sl.id =:designationId) ", nativeQuery = true)
    List<SalaryStructure> findByRejectOrVerifyList(@Param("trnYear") Long trnYear,
                                               @Param("status") String status,
                                               @Param("designationId") Long designationId);


    @Query(value = "SELECT * FROM salary_structure s WHERE s.name =:name order by s.aid desc LIMIT 1", nativeQuery = true)
    Optional<SalaryStructure> findByName(@Param("name") String name);


    @Query(value = "SELECT  s.id , s.name  from salary_structure s WHERE s.applyto = 'dw' AND " +
            " (:name is null or (lower(s.name) LIKE ('%' || lower(:name) || '%')) ) group by s.id, s.name ", nativeQuery = true)
    List<Tuple> wagesDesignationList(@Param("name") String name);
}
