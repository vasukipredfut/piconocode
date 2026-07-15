package com.aria.dao;

import com.aria.dto.LinkSjList;
import com.aria.dto.ProductionDataSetList;
import com.aria.model.JournalProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalProductMappingRepository extends JpaRepository<JournalProductMapping, Long> {

    @Query(value = "SELECT *  FROM journal_product_mapping ad WHERE ad.journal_id =:journalId and ad.product_id =:productId LIMIT 1", nativeQuery = true)
    Optional<JournalProductMapping> findByJournalProductMapping (@Param("journalId") Long journalId, @Param("productId") Long productId);

    @Query(value = "SELECT *  FROM journal_product_mapping ad WHERE ad.journal_id =:journalId and ad.product_id =:productId AND consider_as=:considerAs LIMIT 1", nativeQuery = true)
    Optional<JournalProductMapping> findByJournalProductMapping (@Param("journalId") Long journalId, @Param("productId") Long productId, @Param("considerAs") String considerAs);

    @Query(value = "SELECT *  FROM journal_product_mapping ad WHERE ad.journal_id =:journalId", nativeQuery = true)
    List<JournalProductMapping> findByJournalProductMappingList (@Param("journalId") Long journalId);

    @Query(value = "SELECT jp.product_id as colid , jp.product_name as colName  FROM journal_product_mapping jp "
            + "WHERE (:journalId is null or jp.journal_id = :journalId) "
            +"AND (:branch is null or jp.branch_id = :branch) "
            +"AND (:dept is null or jp.dept_id = :dept) ", nativeQuery = true)
    List<ProductionDataSetList> findByProductionAttributesList(@Param("journalId") Long journalId, @Param("branch") Long branch, @Param("dept") Long dept);

    @Query(value = "SELECT  sj.product_id as productId, p.name as productName, sj.consider_as as category , sj.bom_take_type as bomTakeType, "
            + " sj.bptype as batchProductType, sj.quantity as quantity, sj.cost as cost , sj.fqty as fixQty , sj.pridunit as pridUnit , sj.status as status, "
            + " sj.applicable_For_Jcal as applicableForJcal, sj.bottleweight as bottleWeight "
            + " FROM journal_product_mapping sj LEFT JOIN products_master p ON p.id = sj.product_id "
            + "WHERE sj.status = true  AND sj.consider_as =:cate  AND (:journalId is null or sj.journal_id = :journalId) order by sj.product_id asc", nativeQuery = true)
    List<LinkSjList> findByRawMaterialList(@Param("journalId") Long journalId, @Param("cate") String cate);


    @Query(value = "SELECT *  FROM journal_product_mapping ad " +
            " WHERE ad.journal_id =:journalId and ad.product_id =:productId " +
            " AND consider_as=:considerAs AND ( :bpType is null or bptype =:bpType) LIMIT 1", nativeQuery = true)
    Optional<JournalProductMapping> findByJournalProductMappingANDExcess  (@Param("journalId") Long journalId, @Param("productId") Long productId, @Param("considerAs") String considerAs, @Param("bpType") String  bpType);




    @Query(value = "SELECT *  FROM journal_product_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.product_id =:productId " +
            " AND consider_as=:considerAs " +
            " AND  (bptype IS NULL or bptype not in ('ex'))  LIMIT 1", nativeQuery = true)
    Optional<JournalProductMapping> findByJournalProductMappingNotInEX (@Param("journalId") Long journalId, @Param("productId") Long productId, @Param("considerAs") String considerAs);


    @Query(value = "SELECT *  FROM journal_product_mapping ad WHERE ad.journal_id =:journalId and bptype ='ex' ", nativeQuery = true)
    List<JournalProductMapping> findByJournalProductMappingOnlyExcess  (@Param("journalId") Long journalId);


    @Query(value = "SELECT jp.product_id as colid , jp.product_name as colName  FROM journal_product_mapping jp "
            + " WHERE (:journalId is null or jp.journal_id = :journalId) "
            + " AND (:considerAs is null OR consider_as =:considerAs)"
            + " AND (:branch is null or jp.branch_id = :branch) "
            + " AND (:dept is null or jp.dept_id = :dept) ", nativeQuery = true)
    List<ProductionDataSetList> findByExcessProductionAttributesList(@Param("journalId") Long journalId,
                                                                     @Param("considerAs") String considerAs,
                                                               @Param("branch") Long branch,
                                                               @Param("dept") Long dept);




    @Query(value = "SELECT *  FROM journal_product_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.product_id =:productId " +
            " AND consider_as=:considerAs " +
            " AND ( :inTakeType is null or ad.bom_take_type =:inTakeType ) " +
            " AND ( :bpType is null or bptype =:bpType ) LIMIT 1 ", nativeQuery = true)
    Optional<JournalProductMapping> findByJournalProductMappingANDExcess  (@Param("journalId") Long journalId,
                                                                           @Param("productId") Long productId,
                                                                           @Param("considerAs") String considerAs,
                                                                           @Param("inTakeType") String inTakeType,
                                                                           @Param("bpType") String  bpType);




    @Query(value = "SELECT *  FROM journal_product_mapping ad " +
            " WHERE ad.journal_id =:journalId AND ad.product_id =:productId " +
            " AND consider_as=:considerAs " +
            " AND ( :inTakeType is null or ad.bom_take_type =:inTakeType ) " +
            " AND (bptype IS NULL or bptype not in ('ex'))  LIMIT 1", nativeQuery = true)
    Optional<JournalProductMapping> findByJournalProductMappingNotInEX (@Param("journalId") Long journalId,
                                                                        @Param("productId") Long productId,
                                                                        @Param("considerAs") String considerAs,
                                                                        @Param("inTakeType") String inTakeType );


}
