package com.aria.dao;


import com.aria.dto.ProductionDataSetList;
import com.aria.model.JournalMachineMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalMachineMappingRepository extends JpaRepository<JournalMachineMapping, Long> {

    @Query(value = "SELECT *  FROM journal_machine_mapping ad WHERE ad.journal_id =:journalId and ad.machine_id =:machineId LIMIT 1", nativeQuery = true)
    Optional<JournalMachineMapping> findByJournalMachineMapping (@Param("journalId") Long journalId, @Param("machineId") Long machineId);

    @Query(value = "SELECT *  FROM journal_machine_mapping ad WHERE ad.journal_id =:journalId and ad.machine_id =:machineId and ad.primary_machine_id =:primaryMachineId LIMIT 1", nativeQuery = true)
    Optional<JournalMachineMapping> findByJournalMachineMapping (@Param("journalId") Long journalId, @Param("machineId") Long machineId, @Param("primaryMachineId") Long primaryMachineId);

    @Query(value = "SELECT *  FROM journal_machine_mapping ad WHERE ad.journal_id =:journalId", nativeQuery = true)
    List<JournalMachineMapping> findByJournalMachineMappingList (@Param("journalId") Long journalId);

    @Query(value = "SELECT jm.panel_id as colid , jm.panel_name as colName  FROM journal_machine_mapping jm "
            + "WHERE (:journalId is null or jm.journal_id = :journalId) "
            +"AND (:branch is null or jm.branch_id = :branch) "
            +"AND (:dept is null or jm.dept_id = :dept) ", nativeQuery = true)
    List<ProductionDataSetList> findByProductionAttributesList(@Param("journalId") Long journalId, @Param("branch") Long branch, @Param("dept") Long dept);

    @Query(value = "SELECT *  FROM journal_machine_mapping ad WHERE ad.journal_id =:journalId AND ad.branch_id =:branch ", nativeQuery = true)
    List<JournalMachineMapping> findByJournalMachineMappingList (@Param("journalId") Long journalId, @Param("branch") Long branch);


    @Query(value = "SELECT *  FROM journal_machine_mapping ad WHERE ad.journal_id =:journalId AND ad.primary_machine_id =:primaryMachineId and ad.machine_id <> ad.primary_machine_id", nativeQuery = true)
    List<JournalMachineMapping> findByJournalAuxiliaryMachineList (@Param("journalId") Long journalId, @Param("primaryMachineId") Long primaryMachineId);


    @Query(value = "SELECT *  FROM journal_machine_mapping ad WHERE ad.journal_id =:journalId and (:machineId is null OR ad.machine_id =:machineId) and (:primaryMachineId is null OR ad.primary_machine_id =:primaryMachineId ) LIMIT 1", nativeQuery = true)
    Optional<JournalMachineMapping> findBy(@Param("journalId") Long journalId, @Param("machineId") Long machineId, @Param("primaryMachineId") Long primaryMachineId);

}
