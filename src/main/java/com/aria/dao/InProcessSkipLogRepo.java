package com.aria.dao;

import com.aria.model.InProcessSkipLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InProcessSkipLogRepo extends JpaRepository<InProcessSkipLog, Long> {


}
