package com.aria.dao;


import com.aria.dto.*;
import com.aria.model.BlendingAdvice;
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
public interface BlendingAdviceRepository extends JpaRepository<BlendingAdvice, Long> {

    @Query(value = " select * from blending_advice b where  b.id NOT IN (:Id) AND b.journal_id =:journalId AND b.status = 'A' order by id desc ", nativeQuery = true)
    List<BlendingAdvice> findBy(@Param("Id") Long Id, @Param("journalId") Long journalId);

    @Query(value = "select * from blending_advice where journal_id =:journalId AND blending_detail_type = 'P' order by id desc LIMIT 1", nativeQuery = true)
    Optional<BlendingAdvice> checkBlending(@Param("journalId") Long journalId);


    @Query(value = "select ba.* from blending_advice ba left join blending_advice_details bad on bad.blending_advice_id = ba.id and bad.status = ba.status where ba.journal_id =:journalId AND (:date between ba.from_date and ba.to_date) and ba.status ='A' and bad.branch_id =:branch order by ba.id desc LIMIT 1", nativeQuery = true)
    Optional<BlendingAdvice> findByDate(@Param("date") Date date, @Param("journalId") Long journalId, @Param("branch") Long branch);


    @Query(value = "select * from blending_advice where journal_id =:journalId AND (:date between from_date and to_date) and status ='A' order by id desc LIMIT 1", nativeQuery = true)
    Optional<BlendingAdvice> findByDate(@Param("date") Date date, @Param("journalId") Long journalId);


    @Query(value = "select j.name  as journalName , bad.cas_trn as casTrn,  bad.id as id, ba.id as adviceId, ba.from_date as fromDate , ba.to_date as toDate , pm.id as productId, pm.name as productName, bad.status as status, b.name as branchName from blending_advice ba left join blending_advice_details bad on bad.blending_advice_id = ba.id left join journal j on j.id = bad.journal_id left join products_master pm on pm.id = bad.product_id   "
            + " LEFT JOIN branch b ON b.id = bad.branch_id where  bad.id is Not NULL AND j.id is Not NULL AND pm.id is Not NULL  AND (:journalId is null or ba.journal_id =:journalId ) "
            + "AND (:branch is null or bad.branch_id =:branch) "
            + "AND (:status is null or bad.status =:status) "
            + "AND (cast(:dateFrom as date) is null or (ba.from_date >= :dateFrom)) "
            + "AND (cast(:dateTo as date) is null or (ba.to_date <= :dateTo)) "
            + " order by ba.id desc ", nativeQuery = true)
    Page<BlendingAdvicePrintList> findBy(@Param("journalId") Long journalId,
                                         @Param("dateFrom") Date dateFrom,
                                         @Param("dateTo") Date dateTo,
                                         @Param("status") String status,
                                         @Param("branch") Long branch,
                                         Pageable pageable);


    @Query(value = "select j.name  as journalName , bad.cas_trn as casTrn,  bad.id as id, ba.id as adviceId, ba.from_date as fromDate , ba.to_date as toDate , pm.id as productId, pm.name as productName, bad.status as status, b.name as branchName from blending_advice ba left join blending_advice_details bad on bad.blending_advice_id = ba.id left join journal j on j.id = bad.journal_id left join products_master pm on pm.id = bad.product_id   "
            + " LEFT JOIN branch b ON b.id = bad.branch_id where  bad.id is Not NULL AND j.id is Not NULL AND pm.id is Not NULL AND (bad.status ='A' or bad.status ='P') "
            + " AND  (:journalId is null or ba.journal_id =:journalId ) "
            + " AND (:branch is null or bad.branch_id =:branch) "
            + " AND (cast(:dateFrom as date) is null or (ba.from_date >= :dateFrom)) "
            + " AND (cast(:dateTo as date) is null or (ba.to_date <= :dateTo)) "
            + " order by ba.id desc ", nativeQuery = true)
    Page<BlendingAdvicePrintList> findBy(@Param("journalId") Long journalId,
                                         @Param("dateFrom") Date dateFrom,
                                         @Param("dateTo") Date dateTo,
                                         @Param("branch") Long branch,
                                         Pageable pageable);


    @Query(value = "select ba.* from blending_advice ba WHERE ba.to_date <:date and (ba.status='P' OR ba.status='A')", nativeQuery = true)
    List<BlendingAdvice> findByDate(@Param("date") Date date);

    @Query(value = "select ba.* from blending_advice ba WHERE ba.to_date <:date and ba.status='E' ", nativeQuery = true)
    List<BlendingAdvice> findBy(@Param("date") Date date);

    @Query(value = "select ba.* from blending_advice ba left join blending_advice_details bad on bad.blending_advice_id  = ba.id WHERE ba.to_date <:date and (bad.status='P' OR bad.status='A') ", nativeQuery = true)
    List<BlendingAdvice> findByExpired(@Param("date") Date date);


}
