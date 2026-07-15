package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.QtyMain;

@Repository
public interface QtyMainRepository extends JpaRepository<QtyMain, Long> {

}
