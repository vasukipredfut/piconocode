package com.aria.dao;

import com.aria.dto.SplitList;
import com.aria.model.SplitTransaction;
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
public interface SplitTransactionRepository extends JpaRepository<SplitTransaction, Long> {


    @Query(value = "SELECT max( s.id ) as id, s.refid as productId , max(s.status) as status, max(pm.name) as productName , s.trndate as trnDate , s.trnno as trnNo , s.binname as binNo , max(spd.batch_id) as batchId , max(spd.batch_no) as batchNo "
            + " FROM split_trn s left join split_product_details spd on spd.split_trn_id = s.id left join products_master pm on pm.id = s.refid "
            + " WHERE (:productName is null or lower(pm.name) like ('%' || lower(:productName) || '%')) "
            + " AND (:dept is null OR s.did =:dept) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND (cast(:dateFrom as date) is null or (s.trndate >= :dateFrom)) "
            + " AND (cast(:dateTo as date) is null or (s.trndate <= :dateTo)) "
            + " group by s.refid , s.trndate , s.splitid , s.trnno , s.binname ", nativeQuery = true)
    Page<SplitList> getAllSplitList(@Param("productName") String productName,
                                    @Param("dateFrom") Date dateFrom,
                                    @Param("dateTo") Date dateTo,
                                    @Param("branch") Long branch,
                                    @Param("dept") Long dept,
                                    Pageable pageable);


    @Query(value = "SELECT * FROM split_trn s WHERE  "
            + " (:id is null OR s.id =:id) "
            + " AND (:dept is null OR s.did =:dept) "
            + " AND (:branch is null OR s.bid =:branch) "
            + " AND (:trnNo is null OR s.trnno =:trnNo) ", nativeQuery = true)
    Optional<SplitTransaction> viewSplitTransaction(@Param("id") Long id,
                                                    @Param("branch") Long branch,
                                                    @Param("dept") Long dept,
                                                    @Param("trnNo") String trnNo);

    @Query(value = "SELECT * FROM split_trn s WHERE  "
            + " (:id is null OR s.id =:id) "
            + " AND (:trnNo is null OR s.trnno =:trnNo) ", nativeQuery = true)
    Optional<SplitTransaction> getSplitTransaction(@Param("id") Long id,
                                                    @Param("trnNo") String trnNo);
}

