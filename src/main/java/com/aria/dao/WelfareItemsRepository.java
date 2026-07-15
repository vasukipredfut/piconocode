package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.WelfareItems;

@Repository
public interface WelfareItemsRepository extends JpaRepository<WelfareItems, Long> {

}