package com.aria.dao;

import com.aria.model.MasterField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterFieldRepository extends JpaRepository<MasterField, Long> {

}
