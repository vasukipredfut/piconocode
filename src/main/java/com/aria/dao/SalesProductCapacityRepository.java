package com.aria.dao;

import com.aria.model.SalesProductCapacityEntity;
import com.aria.model.SalesProductGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesProductCapacityRepository extends JpaRepository<SalesProductCapacityEntity,Long> {

}
