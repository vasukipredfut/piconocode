package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.SalesProductGroupEntity;

@Repository
public interface SalesProductGroupRepository extends JpaRepository<SalesProductGroupEntity, Long> {

}
