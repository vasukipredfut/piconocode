package com.aria.dao;

import com.aria.model.SplitMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SplitMasterRepository extends JpaRepository<SplitMaster,Long> {

}
