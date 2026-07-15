package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.WelfareAmount;

@Repository
public interface WelfareAmountRepository extends JpaRepository<WelfareAmount, Long> {

}