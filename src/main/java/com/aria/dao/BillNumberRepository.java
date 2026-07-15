package com.aria.dao;

import com.aria.model.BillNumber;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillNumberRepository extends JpaRepository<BillNumber, Long> {

}