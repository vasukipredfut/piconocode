package com.aria.dao;

import com.aria.model.ConsZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsZoneRepository  extends JpaRepository<ConsZone,Long> {


}
