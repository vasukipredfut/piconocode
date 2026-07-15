package com.aria.dao;


import com.aria.model.JournalBranchMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalBranchMapRepository extends JpaRepository<JournalBranchMap, Long> {


    @Query(value = "SELECT * FROM journal_branch_map ad WHERE ad.journal_id =:journalId AND ad.branch_id =:branchId ", nativeQuery = true)
    Optional<JournalBranchMap> findBy(@Param("journalId") Long journalId, @Param("branchId") Long branchId);

    @Query(value = "SELECT * FROM journal_branch_map ad WHERE ad.journal_id =:journalId AND ad.branch_id NOT IN (:branchId ) ", nativeQuery = true)
    List<JournalBranchMap> findBy(@Param("journalId") Long journalId, @Param("branchId") List<Long> branchId);

    @Query(value = "SELECT * FROM journal_branch_map ad WHERE ad.journal_id =:journalId ", nativeQuery = true)
    List<JournalBranchMap> findBy(@Param("journalId") Long journalId);

}
