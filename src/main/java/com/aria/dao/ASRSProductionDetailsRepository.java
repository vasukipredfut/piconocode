package com.aria.dao;

import com.aria.model.ASRSProductionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ASRSProductionDetailsRepository extends JpaRepository<ASRSProductionDetails,Long> {




    // AND on another field
    boolean existsByTrnNoAndJournalId(String trnNo, Long journalId);

}
