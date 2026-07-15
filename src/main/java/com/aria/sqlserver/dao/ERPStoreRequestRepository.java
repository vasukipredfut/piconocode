package com.aria.sqlserver.dao;

import com.aria.sqlserver.model.ERPStoreRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ERPStoreRequestRepository extends JpaRepository<ERPStoreRequest,Long> {

}
