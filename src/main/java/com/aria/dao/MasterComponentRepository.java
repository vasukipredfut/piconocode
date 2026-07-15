package com.aria.dao;

import com.aria.model.MasterComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterComponentRepository extends JpaRepository<MasterComponent, Long> {

}
