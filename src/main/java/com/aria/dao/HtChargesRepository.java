package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.HtaCharges;

@Repository
public interface HtChargesRepository extends JpaRepository<HtaCharges, Long>  {

}
