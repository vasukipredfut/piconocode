package com.aria.dao;

import com.aria.model.BlendingAdviceDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlendingAdviceDetailsRepository extends JpaRepository<BlendingAdviceDetails, Long> {


    @Query(value = "SELECT  *  FROM blending_advice_details b WHERE  b.branch_id =:branchId ", nativeQuery = true)
    Page<BlendingAdviceDetails> findByBranchId(@Param("branchId") Long branchId, Pageable pageable);

    @Query(value = "SELECT  *  FROM blending_advice_details b WHERE  b.journal_id =:journalId and b.blending_advice_id =:blendingAdviceId Limit 1 ", nativeQuery = true)
    Optional<BlendingAdviceDetails> findBy(@Param("journalId") Long journalId, @Param("blendingAdviceId") Long blendingAdviceId);

    @Query(value = "SELECT  *  FROM blending_advice_details b WHERE  b.journal_id =:journalId and b.blending_advice_id =:blendingAdviceId AND b.branch_id =:branch Limit 1 ", nativeQuery = true)
    Optional<BlendingAdviceDetails> findBy(@Param("journalId") Long journalId, @Param("blendingAdviceId") Long blendingAdviceId, @Param("branch") Long branch);

    @Query(value = "SELECT  *  FROM blending_advice_details b WHERE  b.blending_advice_id =:blendingAdviceId AND b.branch_id =:branchId ", nativeQuery = true)
    List<BlendingAdviceDetails> findByList(@Param("blendingAdviceId") Long blendingAdviceId, @Param("branchId") Long branchId);

    @Query(value = "SELECT  *  FROM blending_advice_details b WHERE  b.blending_advice_id =:blendingAdviceId and (b.status='P' OR b.status='A') ", nativeQuery = true)
    List<BlendingAdviceDetails> findBy(@Param("blendingAdviceId") Long blendingAdviceId);

    @Query(value = "SELECT  *  FROM blending_advice_details b WHERE  b.blending_advice_id =:blendingAdviceId", nativeQuery = true)
    List<BlendingAdviceDetails> findByAdviceId(@Param("blendingAdviceId") Long blendingAdviceId);

    @Query(value = " SELECT  *  FROM blending_advice_details b WHERE  b.blending_advice_id =:blendingAdviceId AND (  b.status NOT IN ('E') AND b.status NOT IN ('A') )  ", nativeQuery = true)
    List<BlendingAdviceDetails> findByNotIn(@Param("blendingAdviceId") Long blendingAdviceId);
}
