package com.aria.dao;

import com.aria.dto.ProductionDataSetList;
import com.aria.model.JournalAttributesMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalAttributesMappingRepo extends JpaRepository<JournalAttributesMapping, Long> {

    @Query(value = "SELECT *  FROM journal_attributes_mapping ad WHERE ad.journal_id =:journalId AND ad.awithid =:typeId AND ad.awith=:awith ", nativeQuery = true)
    List<JournalAttributesMapping> findBy(@Param("journalId") Long journalId, @Param("typeId") Long typeId, @Param("awith") String awith);

    @Query(value = "SELECT *  FROM journal_attributes_mapping ad WHERE ad.journal_id =:journalId AND ad.awithid =:typeId AND ad.attributes_id =:attId AND ad.awith=:awith AND ad.branch_id = :branch AND ad.status=true limit 1", nativeQuery = true)
    Optional<JournalAttributesMapping> findBy(@Param("journalId") Long journalId, @Param("typeId") Long typeId, @Param("attId") Long attId, @Param("awith") String awith, @Param("branch") Long branch);

    @Query(value = "SELECT *  FROM journal_attributes_mapping ad WHERE ad.journal_id =:journalId AND ad.awithid =:typeId AND ad.attributes_id =:attId AND ad.awith=:awith AND ad.branch_id = :branch AND ad.status=false limit 1", nativeQuery = true)
    Optional<JournalAttributesMapping> findByFalse(@Param("journalId") Long journalId, @Param("typeId") Long typeId, @Param("attId") Long attId, @Param("awith") String awith, @Param("branch") Long branch);

    @Query(value = "SELECT *  FROM journal_attributes_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.awithid =:typeId " +
            " AND ad.attributes_id =:attId AND ad.awith=:awith " +
            " AND ad.branch_id = :branch AND ad.consider_as=:considerAs " +
            " AND ad.status =true limit 1", nativeQuery = true)
    Optional<JournalAttributesMapping> findBy(@Param("journalId") Long journalId, @Param("typeId") Long typeId, @Param("attId") Long attId, @Param("awith") String awith, @Param("branch") Long branch, @Param("considerAs") String considerAs);

    @Query(value = "SELECT *  FROM journal_attributes_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.awithid =:typeId " +
            " AND ad.attributes_id =:attId AND ad.awith=:awith " +
            " AND ad.branch_id = :branch AND ad.consider_as=:considerAs " +
            " AND ad.status = false limit 1", nativeQuery = true)
    Optional<JournalAttributesMapping> findByFalse(@Param("journalId") Long journalId, @Param("typeId") Long typeId, @Param("attId") Long attId, @Param("awith") String awith, @Param("branch") Long branch, @Param("considerAs") String considerAs);

    @Query(value = "SELECT *  FROM journal_attributes_mapping ad WHERE ad.journal_id =:journalId AND ad.awithid =:typeId AND ad.attributes_id =:attId AND ad.awith=:awith AND ad.branch_id = :branch limit 1", nativeQuery = true)
    Optional<JournalAttributesMapping> findByUseProductOnly(@Param("journalId") Long journalId, @Param("typeId") Long typeId, @Param("attId") Long attId, @Param("awith") String awith, @Param("branch") Long branch);


    @Query(value = " SELECT  "
            + " pm.id as mapId, pm.attributes_id as colid , pm.attributes_name as colName , pm.type_of_map  as typeOfMap , "
            + " pm.datatype as dataType , pm.awith as awith, pm.awithid as awithId, pm.connect_table as connectTable, "
            + " pm.dispunit as dispUnit, u.name as unitName , pm.minimum_value as minValue , pm.maximum_value as maxValue ,"
            + " pm.consider_as as considerAs , pm.app_cal_status as appCalStatus , pm.bom_intake_type as bomIntakeType "
            + " FROM journal_attributes_mapping pm LEFT JOIN units u ON u.id = pm.dispunit "
            + " WHERE pm.status = true "
            + " AND (:journalId is null or pm.journal_id = :journalId) "
            + " AND (:branch is null or pm.branch_id = :branch) "
            + " AND (:dept is null or pm.dept_id = :dept) "
            + " AND (:withType is null or pm.awith = :withType) "
            + " AND (:withId is null or pm.awithid = :withId) ", nativeQuery = true)
    List<ProductionDataSetList> findByProductionAttributesList(@Param("journalId") Long journalId,
                                                               @Param("branch") Long branch,
                                                               @Param("dept") Long dept,
                                                               @Param("withType") String withType,
                                                               @Param("withId") Long withId);


    @Query(value = "SELECT *  FROM journal_attributes_mapping ad WHERE ad.journal_id =:journalId AND ad.awithid =:typeId AND ad.attributes_id =:attId AND ad.awith=:awith AND ad.branch_id = :branch AND (:considerAs is null OR ad.consider_as=:considerAs) limit 1", nativeQuery = true)
    Optional<JournalAttributesMapping> findByUnique(@Param("journalId") Long journalId, @Param("typeId") Long typeId, @Param("attId") Long attId, @Param("awith") String awith, @Param("branch") Long branch, @Param("considerAs") String considerAs);


    @Query(value = "SELECT *  FROM journal_attributes_mapping ad WHERE ad.journal_id =:journalId AND ad.status = true AND ad.awithid =:typeId AND ad.awith=:awith ", nativeQuery = true)
    List<JournalAttributesMapping> findByTrueList(@Param("journalId") Long journalId, @Param("typeId") Long typeId, @Param("awith") String awith);


    @Query(value = "SELECT *  FROM journal_attributes_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.status = true " +
            " AND ad.awithid =:typeId AND ad.awith=:awith " +
            " AND (:considerAs is null OR ad.consider_as=:considerAs)" +
            " AND (:bomIntakeType is null OR ad.bom_intake_type=:bomIntakeType)", nativeQuery = true)
    List<JournalAttributesMapping> findByProductTrueList(@Param("journalId") Long journalId,
                                                         @Param("typeId") Long typeId,
                                                         @Param("awith") String awith,
                                                         @Param("considerAs") String considerAs,
                                                         @Param("bomIntakeType") String bomIntakeType);


    @Query(value = "SELECT  pm.id as mapId, pm.attributes_id as colid , pm.attributes_name as colName , pm.type_of_map  as typeOfMap ," +
            " pm.datatype as dataType , pm.awith as awith, pm.awithid as awithId, pm.connect_table as connectTable, pm.dispunit as dispUnit," +
            " u.name as unitName , pm.minimum_value as minValue , pm.maximum_value as maxValue , pm.consider_as as considerAs  , " +
            " pm.app_cal_status as appCalStatus  FROM journal_attributes_mapping pm LEFT JOIN units u ON u.id = pm.dispunit "
            + "WHERE pm.status = true "
            + "AND (:journalId is null or pm.journal_id = :journalId) "
            + "AND (:branch is null or pm.branch_id = :branch) "
            + "AND (:considerAs is null or pm.consider_as = :considerAs) "
            + "AND (:dept is null or pm.dept_id = :dept) "
            + "AND (:withType is null or pm.awith = :withType) "
            + "AND (:withId is null or pm.awithid = :withId) ", nativeQuery = true)
    List<ProductionDataSetList> findByExcessProductionAttributesList(@Param("journalId") Long journalId,
                                                                     @Param("branch") Long branch,
                                                                     @Param("dept") Long dept,
                                                                     @Param("considerAs") String considerAs,
                                                                     @Param("withType") String withType,
                                                                     @Param("withId") Long withId);


    @Query(value = "SELECT  *  FROM journal_attributes_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.awithid =:typeId " +
            " AND ad.attributes_id =:attId AND ad.awith=:awith " +
            " AND ad.branch_id = :branch AND ad.consider_as=:considerAs " +
            " AND ad.bom_intake_type =:bomIntakeType AND ad.status = true limit 1", nativeQuery = true)
    Optional<JournalAttributesMapping> findBy(@Param("journalId") Long journalId,
                                              @Param("typeId") Long typeId,
                                              @Param("attId") Long attId,
                                              @Param("awith") String awith,
                                              @Param("branch") Long branch,
                                              @Param("considerAs") String considerAs,
                                              @Param("bomIntakeType") String bomIntakeType);


    @Query(value = "SELECT *  FROM journal_attributes_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.awithid =:typeId " +
            " AND ad.attributes_id =:attId AND ad.awith=:awith " +
            " AND ad.branch_id = :branch AND ad.consider_as=:considerAs " +
            " AND ad.bom_intake_type=:bomIntakeType AND ad.status = false limit 1 ", nativeQuery = true)
    Optional<JournalAttributesMapping> findByFalse(@Param("journalId") Long journalId,
                                                   @Param("typeId") Long typeId,
                                                   @Param("attId") Long attId,
                                                   @Param("awith") String awith,
                                                   @Param("branch") Long branch,
                                                   @Param("considerAs") String considerAs,
                                                   @Param("bomIntakeType") String bomIntakeType);


    @Query(value = "SELECT *  FROM journal_attributes_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.awithid =:typeId " +
            " AND ad.awith=:awith AND ad.consider_as=:considerAs ", nativeQuery = true)
    List<JournalAttributesMapping> findBy(@Param("journalId") Long journalId,
                                          @Param("typeId") Long typeId,
                                          @Param("awith") String awith,
                                          @Param("considerAs") String considerAs);

    @Transactional
    @Modifying
    @Query(value = " Update journal_attributes_mapping ad  SET bom_intake_type =:bomIntakeType " +
            " WHERE ad.journal_id =:journalId AND ad.awithid =:typeId " +
            " AND ad.awith=:awith AND ad.consider_as=:considerAs ", nativeQuery = true)
    void updateBomTakeType(@Param("journalId") Long journalId,
                           @Param("typeId") Long typeId,
                           @Param("awith") String awith,
                           @Param("considerAs") String considerAs,
                           @Param("bomIntakeType") String bomIntakeType);


    @Query(value = "SELECT *  FROM journal_attributes_mapping ad WHERE ad.journal_id =:journalId  ", nativeQuery = true)
    List<JournalAttributesMapping> findBy(@Param("journalId") Long journalId);

}
