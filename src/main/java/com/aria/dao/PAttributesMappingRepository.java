package com.aria.dao;

import com.aria.dto.DataSetList;
import com.aria.dto.ProductionDataSetList;
import com.aria.model.PAttributesMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PAttributesMappingRepository extends JpaRepository<PAttributesMapping, Long> {


    @Query(value = "SELECT *  FROM pattributes_mapping p WHERE p.sjid =:journalId and p.pattributes_id =:pattributesId and p.dept_id=:deptId ", nativeQuery = true)
    Optional<PAttributesMapping> findByPAttributes(@Param("journalId") Long journalId, @Param("deptId") Long deptId, @Param("pattributesId") Long pattributesId);

    @Query(value = "SELECT pm.attributes_id as attributesId, pm.attributes_name as attributesName  , pm.disporder as dispOrder  FROM pattributes_mapping pm WHERE pm.sjid =:journalId and pm.product_id =:productId and pm.machine_id=:machineId ", nativeQuery = true)
    List<DataSetList> findByPAttributesList(@Param("journalId") Long journalId, @Param("productId") Long productId, @Param("machineId") Long machineId);

    @Query(value = "SELECT pm.attributes_id as attributesId, pm.attributes_name as attributesName  , pm.disporder as dispOrder  FROM pattributes_mapping pm WHERE pm.sjid =:journalId ", nativeQuery = true)
    List<DataSetList> findByPAttributesList(@Param("journalId") Long journalId);

    
    @Query(value = "SELECT distinct pm.attributes_id as colid , pm.attributes_name as colName , pm.datatype as dataType ,pm.linkwith as linkWith  FROM pattributes_mapping pm "
            + "WHERE (:journalId is null or pm.sjid = :journalId) AND (:withType is null or pm.awith = :withType) "
            + "AND (:withId is null or pm.awithid = :withId) ", nativeQuery = true)
    List<ProductionDataSetList> findByProductionAttributesList(@Param("journalId") Long journalId, @Param("withType") String withType, @Param("withId") Long withId);


    @Query(value = "SELECT *  FROM pattributes_mapping pm "
            + "WHERE (:journalId is null or pm.sjid = :journalId) AND (:withType is null or pm.awith = :withType) "
            + "AND (:withId is null or pm.awithid = :withId) LIMIT 1 ", nativeQuery = true)
    PAttributesMapping findByPAttributesMapping(@Param("journalId") Long journalId, @Param("withType") String withType, @Param("withId") Long withId);


    @Query(value = "SELECT distinct pm.attributes_id as attributesId, pm.attributes_name as attributesName  , pm.disporder as dispOrder  FROM pattributes_mapping pm WHERE pm.sjid =:journalId and pm.awith = :withType and pm.awithid = :withId ", nativeQuery = true)
    List<DataSetList> findByPAttributesList(@Param("journalId") Long journalId, @Param("withType") String withType, @Param("withId") Long withId);



    @Query(value = "SELECT distinct pm.attributes_id as colid , pm.attributes_name as colName , pm.datatype as dataType , pm.linkwith as linkWith , u.name as unitName FROM pattributes_mapping pm LEFT JOIN units u ON u.id = pm.dispunit "
            + "WHERE (:journalId is null or pm.sjid = :journalId) "
            +"AND (:branch is null or pm.branch = :branch) "
            +"AND (:dept is null or pm.dept = :dept) "
            +"AND (:withType is null or pm.awith = :withType) "
            + "AND (:withId is null or pm.awithid = :withId) ", nativeQuery = true)
    List<ProductionDataSetList> findByProductionAttributesList(@Param("journalId") Long journalId, @Param("branch") Long branch, @Param("dept") Long dept, @Param("withType") String withType, @Param("withId") Long withId);

}
