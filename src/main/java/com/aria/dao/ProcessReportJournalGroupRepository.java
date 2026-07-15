package com.aria.dao;

import com.aria.model.ProcessReportJournalGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessReportJournalGroupRepository extends JpaRepository<ProcessReportJournalGroup, Long> {

}
