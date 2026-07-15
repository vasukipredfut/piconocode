package com.aria.dao;

import com.aria.model.InProcessAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InProcessAuditRepo extends JpaRepository<InProcessAudit, Long> {


}
